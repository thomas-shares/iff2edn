(ns iff2edn.schedule-test
  (:require [clojure.test :refer :all]
            [iff2edn.schedule :refer :all]
            [clojure.pprint :as pprint]
            [java-time :as jt]))


;; Example usage:
(def sample-footnotes
  {"00001" #{(jt/local-date 2024 1 15)
             (jt/local-date 2024 1 16)
             (jt/local-date 2024 1 17)}})

(def large-sample-footnotes
  {"00001" (set (map #(jt/plus (jt/local-date 2024 1 1) (jt/days %)) (range 600)))})

(def sample-journey
  {:service-id 1
   :journey-id 3562
   :footnote "00001"
   :type :ic
   :attributes []
   :stops [{:station :vl
            :arrival-time nil
            :departure-time "1633"
            :arrival-platform "3"
            :departure-platform "3"
            :stop-type :start}
           {:station :br
            :arrival-time "1636"
            :departure-time "1636"
            :arrival-platform "2"
            :departure-platform "2"
            :stop-type :continuation}
           {:station :ut
            :arrival-time "1805"
            :departure-time "1812"
            :arrival-platform "7"
            :departure-platform "7"
            :stop-type :interval}
           {:station :ddr
            :arrival-time "2730"  ; Next day at 03:30
            :departure-time nil
            :arrival-platform "2"
            :departure-platform "2"
            :stop-type :final}]})


#_(def expanded-journeys
  (expand-all-journeys [sample-journey] sample-footnotes))
(def x '({:service-id 1,
            :journey-id 3562,
            :footnote "00001",
            :type :ic,
            :attributes [],
            :stops [],
            :service-date (jt/local-date  "2024-01-15")}))

;;(println  (expand-journey-for-dates sample-journey sample-footnotes) )
;;(println  (doall (lazy-seq x)))
;;(println (= (expand-journey-for-dates sample-journey sample-footnotes) x))
#_(println (jt/local-date  "2024-01-15"))

(deftest test-expand-journey-for-dates
  (testing "Expanding journey for valid dates"
    (is (= (expand-journey-for-dates sample-journey sample-footnotes)
           [{:service-id 1,
            :journey-id 3562,
            :footnote "00001",
            :type :ic,
            :attributes [],
            :stops
            [{:station :vl,
              :arrival-time nil,
              :departure-time
              (jt/local-date-time "2024-01-15T16:33"),
              :arrival-platform "3",
              :departure-platform "3",
              :stop-type :start,
              :arrival-data-time nil}
             {:station :br,
              :arrival-time "1636",
              :departure-time
              (jt/local-date-time  "2024-01-15T16:36"),
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :continuation,
              :arrival-data-time
              (jt/local-date-time  "2024-01-15T16:36")}
             {:station :ut,
              :arrival-time "1805",
              :departure-time
              (jt/local-date-time "2024-01-15T18:12"),
              :arrival-platform "7",
              :departure-platform "7",
              :stop-type :interval,
              :arrival-data-time
              (jt/local-date-time  "2024-01-15T18:05")}
             {:station :ddr,
              :arrival-time "2730",
              :departure-time nil,
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :final,
              :arrival-data-time
              (jt/local-date-time "2024-01-16T03:30")}],
            :service-date (jt/local-date  "2024-01-15")}
           {:service-id 1,
            :journey-id 3562,
            :footnote "00001",
            :type :ic,
            :attributes [],
            :stops
            [{:station :vl,
              :arrival-time nil,
              :departure-time
              (jt/local-date-time  "2024-01-16T16:33"),
              :arrival-platform "3",
              :departure-platform "3",
              :stop-type :start,
              :arrival-data-time nil}
             {:station :br,
              :arrival-time "1636",
              :departure-time
              (jt/local-date-time  "2024-01-16T16:36"),
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :continuation,
              :arrival-data-time
              (jt/local-date-time  "2024-01-16T16:36")}
             {:station :ut,
              :arrival-time "1805",
              :departure-time
              (jt/local-date-time  "2024-01-16T18:12"),
              :arrival-platform "7",
              :departure-platform "7",
              :stop-type :interval,
              :arrival-data-time
              (jt/local-date-time  "2024-01-16T18:05")}
             {:station :ddr,
              :arrival-time "2730",
              :departure-time nil,
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :final,
              :arrival-data-time
              (jt/local-date-time  "2024-01-17T03:30")}],
            :service-date (jt/local-date  "2024-01-16")}
           {:service-id 1,
            :journey-id 3562,
            :footnote "00001",
            :type :ic,
            :attributes [],
            :stops
            [{:station :vl,
              :arrival-time nil,
              :departure-time
              (jt/local-date-time  "2024-01-17T16:33"),
              :arrival-platform "3",
              :departure-platform "3",
              :stop-type :start,
              :arrival-data-time nil}
             {:station :br,
              :arrival-time "1636",
              :departure-time
              (jt/local-date-time  "2024-01-17T16:36"),
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :continuation,
              :arrival-data-time
              (jt/local-date-time "2024-01-17T16:36")}
             {:station :ut,
              :arrival-time "1805",
              :departure-time
              (jt/local-date-time  "2024-01-17T18:12"),
              :arrival-platform "7",
              :departure-platform "7",
              :stop-type :interval,
              :arrival-data-time
              (jt/local-date-time "2024-01-17T18:05")}
             {:station :ddr,
              :arrival-time "2730",
              :departure-time nil,
              :arrival-platform "2",
              :departure-platform "2",
              :stop-type :final,
              :arrival-data-time
              (jt/local-date-time  "2024-01-18T03:30")}],
            :service-date (jt/local-date "2024-01-17")}]))))

;;(clojure.pprint/pprint (expand-journey-for-dates sample-journey sample-footnotes))