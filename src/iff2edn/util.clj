(ns iff2edn.util
  (:require [java-time :as jt]
            [clojure.pprint :as pp]
            [clojure.string :as str])
  (:import [java.time Instant LocalDate LocalTime]))

(defmethod print-method Instant [inst ^java.io.Writer w]
  (.write w (str "#inst/instant \"" (.toString inst) "\"")))

(defmethod print-method LocalDate [date ^java.io.Writer w]
  (.write w (str "#inst/date \"" (jt/format (jt/formatter "yyyy-MM-dd") date) "\"")))

(defmethod print-method LocalTime [time ^java.io.Writer w]
  (.write w (str "#inst/time \"" (jt/format (jt/formatter "HH:mm:ss") time) "\"")))

(defn parse-date-string [date-str]
  "Parse a date string in format 'ddMMyyyy' to an Instant object"
  (let [formatter (jt/formatter "ddMMyyyy")]
    (jt/local-date formatter date-str)))


(defn parse-time-string [time-str]
  "Parse a time string in format 'hhmm' to an Instant object"
  (when time-str
    (let [hours (Integer/parseInt (subs time-str 0 2))
          minutes (Integer/parseInt (subs time-str 2 4))]
      (jt/local-time hours minutes))))

(defn join-date-time [date time]
  "Join a date and time string into a LocalDateTime object."
  (let [date-time (jt/local-date-time date time)
        zone-id (jt/zone-id "UTC")
        zoned (jt/instant date-time zone-id)] 
         zoned))
  

(defn read-lines-to-vector [filename]
  (-> filename
      slurp
      str/split-lines
      vec))



(defn pretty-spit
  [file-name collection]
  (spit (java.io.File. file-name)
        (with-out-str (pp/write collection :dispatch pp/code-dispatch))))
