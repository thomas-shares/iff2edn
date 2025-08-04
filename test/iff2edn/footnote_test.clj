(ns iff2edn.footnote-test
  (:require [clojure.test :refer :all]
            [java-time :as jt]
            [iff2edn.footnote :refer :all]
            [iff2edn.util :as util]))

;; Helper for creating a LocalDate
(defn d [yyyy mm dd]
  (jt/local-date yyyy mm dd))

(deftest string-date-vector-test
  (testing 
   (is (= (string-date-vector "110010101" (util/parse-date-string "01082025"))
          #{(jt/local-date "2025-08-01")
            (jt/local-date "2025-08-02")
            (jt/local-date "2025-08-05")
            (jt/local-date "2025-08-07")
            (jt/local-date "2025-08-09")}))))


(deftest test-string-date-vector
  (let [start (d 2024 1 1)]
    (is (= #{(d 2024 1 1) (d 2024 1 3)}
           (string-date-vector "101" start)))
    (is (= #{}
           (string-date-vector "000" start)))
    (is (= #{start}
           (string-date-vector "100" start)))
    (is (= #{(jt/plus start (jt/days 1))}
           (string-date-vector "010" start))))
  
  (testing "Throws for invalid character"
    (is (thrown-with-msg?
         IllegalArgumentException
         #"Invalid character"
         (string-date-vector "10a" (d 2024 1 1))))))

(deftest test-string-date-vector
  (let [start (d 2024 1 1)]
    (is (= #{(d 2024 1 1) (d 2024 1 3)}
           (string-date-vector "101" start)))
    (is (= #{}
           (string-date-vector "000" start)))
    (is (= #{start}
           (string-date-vector "100" start)))
    (is (= #{(jt/plus start (jt/days 1))}
           (string-date-vector "010" start))))
  
  (testing "Throws for invalid character"
    (is (thrown-with-msg?
         IllegalArgumentException
         #"Invalid character"
         (string-date-vector "10a" (d 2024 1 1))))))

(deftest convert-footnote-test
  (testing "Converts footnote to expected format"
    (let [footnote ["This is a first line""#00001""01001""#00002""1011000"]]
      (is (= {"00001" #{(jt/local-date "2024-01-02") (jt/local-date "2024-01-05")}
              "00002" #{(jt/local-date "2024-01-01") (jt/local-date "2024-01-03") (jt/local-date "2024-01-04")}}
             (convert-footnotes footnote (jt/local-date "2024-01-01")))))))