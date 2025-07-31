(ns iff2edn.time-table
  (:require [iff2edn.util :as util]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(defn parse-delivery [line]
  ;;(println "Parsing delivery line:" line)
  (let [parts (str/split line #",")]
    {:company-number (Integer/parseInt (subs (first parts) 1))
     :start-date (util/parse-date-string (second parts))
     :end-date (util/parse-date-string (nth parts 2))}))

(defn parse-time [time-str]
  "Parse time string to a java.time.LocalTime instance"
  (if (and time-str (not= time-str "9999"))
    (str/trim time-str)
    nil))


#_(defn parse-time [time-str]
  "Parse time string to a java.time.LocalTime instance"
  (when (and time-str (not= time-str "9999"))
    (let [time-int (Integer/parseInt (str/trim time-str))
          hours (quot time-int 100)
          minutes (mod time-int 100)]
      ;; Handle times after midnight (e.g., 25:00 becomes next day 01:00)
      (if (>= hours 24)
        (java.time.LocalTime/of (- hours 24) minutes)
        (java.time.LocalTime/of hours minutes)))))

(defn parse-service-identification [line]
  "Parse service identification record (#)"
  (let [service-id (subs line 1)]
    {:service-id (Integer/parseInt service-id)}))

(defn parse-service-number [line]
  "Parse service number record (%)"
  (let [parts (str/split line #",")
        company-num (Integer/parseInt (subs (first parts) 1))
        service-num (Integer/parseInt (second parts))
        variant (nth parts 2)
        first-stop (Integer/parseInt (nth parts 3))
        last-stop (Integer/parseInt (nth parts 4))
        service-name (if (> (count parts) 5) (nth parts 5) "")]
    {:company-number company-num
     :service-number service-num
     :variant variant
     :first-stop first-stop
     :last-stop last-stop
     :service-name service-name}))

(defn parse-validity [line]
  "Parse validity record (-)"
  (let [parts (str/split line #",")
        footnote-num (subs (first parts) 1)
        first-stop (Integer/parseInt (second parts))
        last-stop (Integer/parseInt (nth parts 2))]
    {:footnote-number footnote-num
     :first-stop first-stop
     :last-stop last-stop}))

(defn parse-transport-mode [line]
  "Parse transport mode record (&)"
  (let [parts (str/split line #",")
        mode-code (subs (first parts) 1)
        first-stop (Integer/parseInt (second parts))
        last-stop (Integer/parseInt (nth parts 2))]
    {:transport-mode-code (keyword (str/trim (str/lower-case mode-code)))
     :first-stop first-stop
     :last-stop last-stop}))

(defn parse-attribute [line]
  "Parse attribute record (*)"
  (let [parts (str/split line #",")
        attr-code (subs (first parts) 1)
        first-stop (Integer/parseInt (second parts))
        last-stop (Integer/parseInt (nth parts 2))]
    {:attribute-code attr-code
     :first-stop first-stop
     :last-stop last-stop}))

(defn parse-start-record [line]
  "Parse start record (>)"
  (let [parts (str/split line #",")
        station (subs (first parts) 1)
        departure-time (parse-time (second parts))]
    {:type :start
     :station (keyword (str/trim station))
     :departure-time departure-time}))

(defn parse-continuation-record [line]
  "Parse continuation record (.)"
  (let [parts (str/split line #",")
        station (subs (first parts) 1)
        time (parse-time (second parts))]
    {:type :continuation
     :station (keyword (str/trim station))
     :time time}))

(defn parse-passing-record [line]
  "Parse passing record (;)"
  (let [station (subs line 1)]
    {:type :passing
     :station (keyword (str/trim station))}))

(defn parse-interval-record [line]
  "Parse interval record (+)"
  (let [parts (str/split line #",")
        station (subs (first parts) 1)
        arrival-time (parse-time (second parts))
        departure-time (parse-time (nth parts 2))]
    {:type :interval
     :station (keyword (str/trim station))
     :arrival-time arrival-time
     :departure-time departure-time}))

(defn parse-platform-record [line]
  "Parse platform record (?)"
  (let [parts (str/split line #",")
        arr-platform (str/trim (subs (first parts) 1))
        dep-platform (str/trim (second parts))
        footnote (if (> (count parts) 2) 
                   (Integer/parseInt (nth parts 2)) 
                   0)]
    {:type :platform
     :arrival-platform (when (not= arr-platform "") arr-platform)
     :departure-platform (when (not= dep-platform "") dep-platform)
     :footnote footnote}))

(defn parse-final-record [line]
  "Parse final record (<)"
  (let [parts (str/split line #",")
        station (subs (first parts) 1)
        arrival-time (parse-time (second parts))]
    {:type :final
     :station (keyword (str/trim station))
     :arrival-time arrival-time}))

(defn parse-line [line]
  "Parse a single line based on its record type"
  (when (and line (not (str/blank? line)))
    (let [record-type (first line)]
      (case record-type
        \@ {:type :delivery :data (parse-delivery line)}
        \# {:type :service-identification :data (parse-service-identification line)}
        \% {:type :service-number :data (parse-service-number line)}
        \- {:type :validity :data (parse-validity line)}
        \& {:type :transport-mode :data (parse-transport-mode line)}
        \* {:type :attribute :data (parse-attribute line)}
        \> {:type :stop :data (parse-start-record line)}
        \. {:type :stop :data (parse-continuation-record line)}
        \; {:type :stop :data (parse-passing-record line)}
        \+ {:type :stop :data (parse-interval-record line)}
        \? {:type :platform :data (parse-platform-record line)}
        \< {:type :stop :data (parse-final-record line)}
        {:type :unknown :raw line}))))

(defn build-journey-edn [journey-lines]
  "Build simplified EDN structure for a single journey from its lines"
  (let [parsed-lines (map parse-line journey-lines)
        service-id-line (first (filter #(= (:type %) :service-identification) parsed-lines))
        transport-modes (filter #(= (:type %) :transport-mode) parsed-lines)
        attributes (filter #(= (:type %) :attribute) parsed-lines)
        stops (filter #(and  (= (:type %) :stop) (not (= (get-in % [:data :type]) :passing))) parsed-lines)
        platforms (filter #(= (:type %) :platform) parsed-lines)
        service-number (first (filter #(= (:type %) :service-number) parsed-lines))
        validity (first (filter #(= (:type %) :validity) parsed-lines))
        ;;delivery (first (filter #(= (:type %) :delivery) parsed-lines))

        ;;_ (println "STOPS    " )
        
        ;; Group platforms by their position in the file (they follow stops) and we removed stops with passing type
        ;; so we can safely assume that platforms are in the same order as stops
        platform-map (into {} (map-indexed (fn [idx platform] 
                                             [idx (:data platform)]) 
                                           platforms))
        
        ;; Build stops with platform information
        stops-with-platforms 
        (into [] (map-indexed 
          (fn [idx stop-record]
            (let [stop-data (:data stop-record)
                  platform-data (get platform-map idx)
                  stop-type (:type stop-data)]
              {:station (:station stop-data)
               :arrival-time (cond
                              (= stop-type :start) nil
                              (= stop-type :continuation) (:time stop-data)
                              (= stop-type :interval) (:arrival-time stop-data)
                              (= stop-type :final) (:arrival-time stop-data)
                              (= stop-type :passing) nil
                              :else nil)
               :departure-time (cond
                               (= stop-type :start) (:departure-time stop-data)
                               (= stop-type :continuation) (:time stop-data)
                               (= stop-type :interval) (:departure-time stop-data)
                               (= stop-type :final) nil
                               (= stop-type :passing) nil
                               :else nil)
               :arrival-platform (:arrival-platform platform-data)
               :departure-platform (:departure-platform platform-data)
               :stop-type stop-type}))
          stops))]
    
    {:service-id (-> service-id-line :data :service-id)
     :journey-id (-> service-number :data :service-number)
     ;;:types (mapv #(-> % :data :transport-mode-code) transport-modes)
     :footnote (-> validity :data :footnote-number)
     :type (-> transport-modes first :data :transport-mode-code)
     :attributes (mapv #(-> % :data :attribute-code) attributes)
     :stops stops-with-platforms}))

(defn journey-belongs-to-company? [journey-lines company-number]
  "Check if a journey belongs to a specific company by examining service number records"
  (let [parsed-lines (map parse-line journey-lines)
        service-numbers (filter #(= (:type %) :service-number) parsed-lines)]
    (some #(= (:company-number (:data %)) company-number) service-numbers)))

(defn process-lines-with-company-filter
  ([lines company-number] (process-lines-with-company-filter lines company-number nil))
  ([lines company-number current-journey]
   (lazy-seq
     (if-let [line (first lines)]
       (let [parsed-line (parse-line line)]
         (cond
           ;; Start of new journey (service identification record)
           (= (:type parsed-line) :service-identification)
           (if current-journey
             ;; Check if previous journey belongs to company and finish it
             (let [finished-journey (when (journey-belongs-to-company? current-journey company-number)
                                      (build-journey-edn current-journey))]
               (if finished-journey
                 (cons finished-journey
                       (process-lines-with-company-filter (rest lines) company-number [line]))
                 (process-lines-with-company-filter (rest lines) company-number [line])))
             ;; Start first journey
             (process-lines-with-company-filter (rest lines) company-number [line]))

           ;; Skip delivery record at file start
           (= (:type parsed-line) :delivery)
           (process-lines-with-company-filter (rest lines) company-number current-journey)

           ;; Add line to current journey
           current-journey
           (process-lines-with-company-filter (rest lines) company-number (conj current-journey line))

           ;; No current journey and not a start line - skip
           :else
           (process-lines-with-company-filter (rest lines) company-number current-journey)))

       ;; End of lines - finish last journey if exists and belongs to company
       (when (and current-journey (journey-belongs-to-company? current-journey company-number))
         [(build-journey-edn current-journey)])))))


(defn process-file [filename company-number]
  (with-open [rdr (clojure.java.io/reader filename)]
    (doall (process-lines-with-company-filter (line-seq rdr) company-number))))

;;(util/pretty-spit  "times.edn" (take 5 (process-file "resources/timetbls_new.dat" 100)))

(comment
 (take 5 (process-file "./resources/timetbls_new.dat" 100)) 
 )