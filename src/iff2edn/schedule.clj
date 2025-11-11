(ns iff2edn.schedule
  (:require [iff2edn.util :as util]
            [java-time :as jt]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]])
  (:import [java.time Instant]))

(defn parse-time
  "Parse IFF time format (HHMM) to minutes since midnight.
   Handles 24+ hour format for next-day times."
  [time-str]
  (when time-str
    (let [hours (Integer/parseInt (subs time-str 0 2))
          minutes (Integer/parseInt (subs time-str 2 4))]
      (+ (* hours 60) minutes))))

(defn minutes-to-time-and-date-offset
  "Convert minutes since midnight to [time-str date-offset].
   date-offset is 0 for same day, 1 for next day, etc."
  [minutes]
  (let [day-minutes (* 24 60)
        date-offset (quot minutes day-minutes)
        time-minutes (mod minutes day-minutes)
        hours (quot time-minutes 60)
        mins (mod time-minutes 60)]
    [(format "%02d%02d" hours mins) date-offset]))

(defn add-actual-dates-to-stop
  "Add actual arrival and departure dates to a stop based on service date and time offsets."
  [stop service-date]
  (let [arrival-minutes (parse-time (:arrival-time stop))
        departure-minutes (parse-time (:departure-time stop))
        _ (println "H: " arrival-minutes departure-minutes)

        [arrival-time arrival-offset] (when arrival-minutes
                                        (minutes-to-time-and-date-offset arrival-minutes))
        [departure-time departure-offset] (when departure-minutes
                                            (minutes-to-time-and-date-offset departure-minutes))

        ;;arrival-date (when arrival-offset
        ;;               (jt/plus service-date (jt/days arrival-offset)))
        ;;departure-date (when departure-offset
        ;;                 (jt/plus service-date (jt/days departure-offset)))
        
        arrival-time (when arrival-time
                            (util/parse-time-string arrival-time))
        departure-time (when departure-time
                              (util/parse-time-string departure-time))]
    (println "actual: " arrival-time departure-time)

    (assoc stop
           :arrival-time arrival-time
           :departure-time departure-time)))

(defn expand-journey-for-dates
  "Expand a single journey for all valid dates from its footnote."
  [journey footnotes]
  (let [footnote-id (:footnote journey)
        valid-dates (get footnotes footnote-id #{})]

    (for [service-date valid-dates]
      (-> journey
          (assoc :service-date service-date)
          (update :stops
                  (fn [stops]
                    (mapv #(add-actual-dates-to-stop % service-date) stops)))))))

(defn expand-all-journeys
  "Expand all journeys based on their footnote validity."
  [journeys footnotes]
  (mapcat #(expand-journey-for-dates % footnotes) journeys))

(defn date->instant [date]
  (when (not (nil? date))
    (let [zone-id    (java.time.ZoneId/of "UTC")      ; or "UTC"
          zdt        (.atStartOfDay date zone-id)
          instant    (.toInstant zdt)]
      instant)))

(defn date-time->instant [date-time]
  (println "date-time->instant:   " date-time (type date-time))
  (if (not (nil? date-time))
    (let [zone-id    (java.time.ZoneId/of "UTC")      ; or "UTC"
          _ (println "zone-id: " zone-id   " date-time: " date-time)
          zdt        (.atZone date-time zone-id)
          instant    (.toInstant zdt)]
       instant)
    nil))

(if (not (nil?  5))
  "t"
  "f")


(def m {:station :rtd
 :arrival-time nil
 :actual-arrival-time nil
 :platform "3a"
 :departure-time (jt/local-date-time "2025-08-06T23:35")
 :actual-departure-time (jt/local-date-time "2025-08-06T23:35")
 :expected-passenger-count 123})

(update m :departure-time date-time->instant)



(defn convert-journey [journey]
  (let  [journey-id (:journey-id journey)
         ;;date (date->instant (:service-date journey))
         date (:service-date journey)
         id (str (str/replace (:service-date journey) #"-" "") journey-id)
         type (:type journey)
         stops (->> (:stops journey)
                    (map #(assoc % :platform (:arrival-platform %)
                                 :actual-arrival-time nil
                                 :actual-departure-time nil
                                 :expected-passenger-count 0
                         :arrival-time (:arrival-date-time %)
                         :departure-time (:departure-date-time %)))
                    #_(map #(update % :departure-time date-time->instant))
                    #_(map #(update % :arrival-time date-time->instant))
                    (map #(dissoc % :arrival-date-time :departure-platform :arrival-platform :departure-date-time)))
         all (select-keys journey [:type :journey-id ])]
    #_(println "hier: " stops)
    (assoc all 
           :id id
           :date date
           :stops stops
           :type type
           :status :planned
           :length nil
           :length-meters nil
           :invalid-reason ""
           :next-station-idx 0
           :current-delay nil
           :train-sets []
           :current-location nil
           :previous-locations [])))

#_(defmethod print-method Instant [inst ^java.io.Writer w]
  (.write w (str "#inst/instant \"" (.toString inst) "\"")))

(comment
  
;;(spit "/home/thomas/pretty-all.edn" (prn-str (pmap #(convert-journey %) (expand-all-journeys iff2edn.time-table/tm iff2edn.footnote/footn))))

  
(take 5 (map-indexed  (fn [idx e] (util/pretty-spit (str "/home/thomas/tmp/journey-" idx ".edn" ) (prn-str e)))
              (pmap #(convert-journey %) 
                    (expand-all-journeys 
                     iff2edn.time-table/tm 
                     iff2edn.footnote/footn))))

  

#_(def journey-1
  {:service-id 1, 
   :journey-id 4025, 
   :footnote "00001", 
   :type :spr, 
   :attributes [], 
   :stops [{:station :utg,
            :arrival-time nil,
            :departure-time "0648",
            :arrival-platform "1b",
            :departure-platform "1b",
            :stop-type :start,
            :arrival-date-time nil,
            :departure-date-time #object[java.time.Instant 0x3afb579a "2025-08-01T06:48:00Z"]}
           {:station :kma, :arrival-time "0652",
            :departure-time "0652",
            :arrival-platform "2",
            :departure-platform "2",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x12b4603 "2025-08-01T06:52:00Z"],
            :departure-date-time #object[java.time.Instant 0x341f1677 "2025-08-01T06:52:00Z"]}
           {:station :wm,
            :arrival-time "0656",
            :departure-time "0656",
            :arrival-platform "3",
            :departure-platform "3",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x2069e65a "2025-08-01T06:56:00Z"],
            :departure-date-time #object[java.time.Instant 0x40ce224a "2025-08-01T06:56:00Z"]}
           {:station :zzs,
            :arrival-time "0659",
            :departure-time "0659",
            :arrival-platform "2",
            :departure-platform "2",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x1b0e7f0e "2025-08-01T06:59:00Z"],
            :departure-date-time #object[java.time.Instant 0x576e7eb6 "2025-08-01T06:59:00Z"]}
           {:station :kz,
            :arrival-time "0702",
            :departure-time "0702",
            :arrival-platform "2",
            :departure-platform "2",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x385a0937 "2025-08-01T07:02:00Z"],
            :departure-date-time #object[java.time.Instant 0x937b004 "2025-08-01T07:02:00Z"]}
           {:station :zd,
            :arrival-time "0705",
            :departure-time "0705",
            :arrival-platform "5",
            :departure-platform "5",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x61a94823 "2025-08-01T07:05:00Z"],
            :departure-date-time #object[java.time.Instant 0x4596116e "2025-08-01T07:05:00Z"]}
           {:station :ass,
            :arrival-time "0711",
            :departure-time "0711",
            :arrival-platform "6",
            :departure-platform "6",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x2dedab5a "2025-08-01T07:11:00Z"],
            :departure-date-time #object[java.time.Instant 0x19ee9973 "2025-08-01T07:11:00Z"]}
           {:station :asd,
            :arrival-time "0717",
            :departure-time "0719",
            :arrival-platform "2b",
            :departure-platform "2b",
            :stop-type :interval,
            :arrival-date-time #object[java.time.Instant 0x322d223d "2025-08-01T07:17:00Z"],
            :departure-date-time #object[java.time.Instant 0x47455877 "2025-08-01T07:19:00Z"]}
           {:station :asdm,
            :arrival-time "0724",
            :departure-time "0724",
            :arrival-platform "9",
            :departure-platform "9",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x7329cbe "2025-08-01T07:24:00Z"],
            :departure-date-time #object[java.time.Instant 0x6a7aa82a "2025-08-01T07:24:00Z"]}
           {:station :asa,
            :arrival-time "0728",
            :departure-time "0728",
            :arrival-platform "4",
            :departure-platform "4",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x311d4392 "2025-08-01T07:28:00Z"],
            :departure-date-time #object[java.time.Instant 0x5cf63326 "2025-08-01T07:28:00Z"]}
           {:station :dvd,
            :arrival-time "0731",
            :departure-time "0731",
            :arrival-platform "8",
            :departure-platform "8",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x4f32936a "2025-08-01T07:31:00Z"],
            :departure-date-time #object[java.time.Instant 0x7036495e "2025-08-01T07:31:00Z"]}
           {:station :asb,
            :arrival-time "0734",
            :departure-time "0734",
            :arrival-platform "6",
            :departure-platform "6",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x2b6c8519 "2025-08-01T07:34:00Z"],
            :departure-date-time #object[java.time.Instant 0x466efc51 "2025-08-01T07:34:00Z"]}
           {:station :ashd,
            :arrival-time "0736",
            :departure-time "0736",
            :arrival-platform "5",
            :departure-platform "5",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x1d9c8e35 "2025-08-01T07:36:00Z"],
            :departure-date-time #object[java.time.Instant 0x5fa25861 "2025-08-01T07:36:00Z"]}
           {:station :ac,
            :arrival-time "0740",
            :departure-time "0740",
            :arrival-platform "3",
            :departure-platform "3",
            :stop-type :continuation,
            :arrival-date-time #object[java.time.Instant 0x10294ac1 "2025-08-01T07:40:00Z"],
            :departure-date-time #object[java.time.Instant 0x2114b0e1 "2025-08-01T07:40:00Z"]}
           {:station :bkl, :arrival-time "0748", :departure-time "0748", :arrival-platform "3", :departure-platform "3", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x58806fd0 "2025-08-01T07:48:00Z"], :departure-date-time #object[java.time.Instant 0x152c45f1 "2025-08-01T07:48:00Z"]} 
           {:station :wd, :arrival-time "0756", :departure-time "0758", :arrival-platform "2", :departure-platform "2", :stop-type :interval, :arrival-date-time #object[java.time.Instant 0x4f097ad7 "2025-08-01T07:56:00Z"], :departure-date-time #object[java.time.Instant 0x4b9e586f "2025-08-01T07:58:00Z"]}
           {:station :gdg, :arrival-time "0807", :departure-time "0807", :arrival-platform "3", :departure-platform "3", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x10bb4023 "2025-08-01T08:07:00Z"], :departure-date-time #object[java.time.Instant 0x67cf9c08 "2025-08-01T08:07:00Z"]}
           {:station :gd, :arrival-time "0810", :departure-time "0811", :arrival-platform "8", :departure-platform "8", :stop-type :interval, :arrival-date-time #object[java.time.Instant 0x63149de4 "2025-08-01T08:10:00Z"], :departure-date-time #object[java.time.Instant 0x5af0fcc9 "2025-08-01T08:11:00Z"]}
           {:station :nwk, :arrival-time "0818", :departure-time "0818", :arrival-platform "2", :departure-platform "2", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x100015ec "2025-08-01T08:18:00Z"], :departure-date-time #object[java.time.Instant 0xbb4e438 "2025-08-01T08:18:00Z"]} 
           {:station :cps, :arrival-time "0821", :departure-time "0821", :arrival-platform "2", :departure-platform "2", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x4c08b317 "2025-08-01T08:21:00Z"], :departure-date-time #object[java.time.Instant 0x11c308eb "2025-08-01T08:21:00Z"]} 
           {:station :rta, :arrival-time "0824", :departure-time "0824", :arrival-platform "2", :departure-platform "2", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x17cd04a "2025-08-01T08:24:00Z"], :departure-date-time #object[java.time.Instant 0x2dc56179 "2025-08-01T08:24:00Z"]} 
           {:station :rtn, :arrival-time "0828", :departure-time "0828", :arrival-platform "2", :departure-platform "2", :stop-type :continuation, :arrival-date-time #object[java.time.Instant 0x24dcffdd "2025-08-01T08:28:00Z"], :departure-date-time #object[java.time.Instant 0x2d513ffd "2025-08-01T08:28:00Z"]} 
           {:station :rtd, :arrival-time "0834", :departure-time nil, :arrival-platform "16", :departure-platform "16", :stop-type :final, :arrival-date-time #object[java.time.Instant 0x4a48549d "2025-08-01T08:34:00Z"], :departure-date-time nil}], :service-date #object[java.time.LocalDate 0x4c803a04 "2025-08-01"]}
)


  (def j {:service-id 1
          :journey-id 3562
          :type :spr
          :service-date (jt/local-date "2025-08-01")
          :stops [{:station :utg,
                   :arrival-time nil,
                   :departure-time "0648",
                   :arrival-platform "1b",
                   :departure-platform "1b",
                   :stop-type :start,
                   :arrival-date-time nil,
                   :departure-date-time (Instant/parse "2025-08-01T06:48:00Z")}
                  {:station :asd,
                   :arrival-time "0717",
                   :departure-time "0719",
                   :arrival-platform "2b",
                   :departure-platform "2b",
                   :stop-type :interval,
                   :arrival-date-time  (Instant/parse "2025-08-01T07:17:00Z"),
                   :departure-date-time  (Instant/parse "2025-08-01T07:19:00Z")}
                  {:station :rtd,
                   :arrival-time "0834",
                   :departure-time nil,
                   :arrival-platform "16",
                   :departure-platform "16",
                   :stop-type :final,
                   :arrival-date-time  (Instant/parse  "2025-08-01T08:34:00Z"),
                   :departure-date-time nil}],
          })
(pprint (convert-journey j))

  )

