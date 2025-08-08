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

        [arrival-time arrival-offset] (when arrival-minutes
                                        (minutes-to-time-and-date-offset arrival-minutes))
        [departure-time departure-offset] (when departure-minutes
                                            (minutes-to-time-and-date-offset departure-minutes))

        arrival-date (when arrival-offset
                       (jt/plus service-date (jt/days arrival-offset)))
        departure-date (when departure-offset
                         (jt/plus service-date (jt/days departure-offset)))
        arrival-date-time (when (and arrival-date arrival-time)
                            (util/join-date-time arrival-date (util/parse-time-string arrival-time)))
        departure-date-time (when (and departure-date departure-time)
                              (util/join-date-time departure-date (util/parse-time-string departure-time)))]

    (assoc stop
           :arrival-date-time arrival-date-time
           :departure-date-time departure-date-time)))

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
  (println "date-time->instant:   " date-time)
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
         date (date->instant (:service-date journey))
         id (str (str/replace (:service-date journey) #"-" "") journey-id)
         stops (->> (:stops journey)
                    (map #(assoc % :platform (:arrival-platform %) :actual-arrival-time nil :actual-departure-time nil))
                    (map #(update % :departure-time date-time->instant))
                    (map #(update % :arrival-time date-time->instant))
                    (map #(dissoc % :arrival-data-time :departure-platform :arrival-platform :stop-type))
                    (println "stops!!!: " )
                    )
         all (select-keys journey [:type :journey-id ])]
    #_(pprint stops)
    (assoc all :id id :date date :stops stops)))

(comment
  
(expand-all-journeys iff2edn.time-table/tm iff2edn.footnote/footn)


  (def j {:service-id 1
          :journey-id 3562
          :service-date (jt/local-date "2024-01-17")
          :stops [{:station :vl,
                   :arrival-time nil,
                   :departure-time
                   (jt/local-date-time  "2024-01-17T16:33"),
                   :arrival-platform "3",
                   :departure-platform "3",
                   :stop-type :start,
                   :arrival-data-time nil}
                  {:station :ddr,
                   :arrival-time "2730",
                   :departure-time nil,
                   :arrival-platform "2",
                   :departure-platform "2",
                   :stop-type :final,
                   :arrival-data-time
                   (jt/local-date-time  "2024-01-18T03:30")}]})
(convert-journey j)

  )

