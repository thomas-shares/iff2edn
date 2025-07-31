(ns iff2edn.util-test
  (:require [clojure.test :refer :all]
            [java-time :as jt]
            [iff2edn.util :refer :all]))

#_(def q (read-footnote (rest (read-lines-to-vector "resources/data/footnote.dat"))
                        (parse-date-string "21072025")))

(deftest parse-date-string-test
  (testing
   (is (= (parse-date-string "31122025") (jt/local-date "2025-12-31")))
    (is (= (parse-date-string "01012026") (jt/local-date "2026-01-01")))
    #_(is (thrown? IllegalArgumentException (parse-date-string "30022025")))))


(deftest parse-time-string-test
  (testing
   (is (= (parse-time-string "1534") (jt/local-time 15 34)))
   (is (= (parse-time-string nil) nil))
   (is (= (parse-time-string "0000") (jt/local-time 0 0 )))))