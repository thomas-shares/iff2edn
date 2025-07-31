(ns iff2edn.footnote-test
  (:require [clojure.test :refer :all]
            [java-time :as jt]
            [iff2edn.footnote :refer :all]
            [iff2edn.util :as util]))


(deftest string-date-vector-test
  (testing 
   (is (= (string-date-vector "110010101" (util/parse-date-string "01082025"))
          #{(jt/local-date "2025-08-01")
            (jt/local-date "2025-08-02")
            (jt/local-date "2025-08-05")
            (jt/local-date "2025-08-07")
            (jt/local-date "2025-08-09")}))))

