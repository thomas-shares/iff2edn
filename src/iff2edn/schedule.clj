(ns iff2edn.schedule
  (:require [iff2edn.util :as util]
            [java-time :as jt]))

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
           :arrival-data-time arrival-date-time
           :departure-time departure-date-time)))

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

