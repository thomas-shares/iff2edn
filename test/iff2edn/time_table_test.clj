(ns iff2edn.time-table-test
  (:require [clojure.test :refer :all]
            [java-time :as jt]
            [iff2edn.time-table :refer :all]))

(deftest parse-delivery-test
  (testing "Parse delivery record"
    (is (= (parse-delivery "@123,01012020,31122020")
           {:company-number 123
            :start-date (jt/local-date "2020-01-01")
            :end-date (jt/local-date "2020-12-31")}))
    (is (= (parse-delivery "@456,01012021,31122021")
           {:company-number 456
            :start-date (jt/local-date "2021-01-01")
            :end-date (jt/local-date "2021-12-31")}))))


(deftest parse-time-test
  (testing "Parse time string to LocalTime"
    (is (= (parse-time "0100  ") "0100"))
    (is (= (parse-time "   1534") "1534"))
    (is (= (parse-time "2500") "2500")))) ; Testing wrap around midnight


(deftest parse-service-identification-test
  (testing "Parse service identification record"
    (is (= (parse-service-identification "#123") {:service-id 123}))
    (is (= (parse-service-identification "#000256") {:service-id 256}))))

(deftest parse-service-number-test
  (testing "Parse service number record"
    (is (= (parse-service-number "%123,456,variant,1,2,Service Name")
           {:company-number 123
            :service-number 456
            :variant "variant"
            :first-stop 1
            :last-stop 2
            :service-name "Service Name"}))
    (is (= (parse-service-number "%001,002,variant2,3,4,Another Service")
           {:company-number 1
            :service-number 2
            :variant "variant2"
            :first-stop 3
            :last-stop 4
            :service-name "Another Service"}))))

(deftest parse-validity-test
  (testing "Parse validity record"
    (is (= (parse-validity "-000001,2,3") {:footnote-number "000001"
                                      :first-stop 2
                                      :last-stop 3}))
    (is (= (parse-validity "-10,20,30") {:footnote-number "10"
                                         :first-stop 20
                                         :last-stop 30}))))
(deftest parse-transport-mode-test
  (testing "Parse transport mode record"
    (is (= (parse-transport-mode "&bus,1,2") {:transport-mode-code :bus
                                              :first-stop 1
                                              :last-stop 2}))
    (is (= (parse-transport-mode "&train,3,4") {:transport-mode-code :train
                                                :first-stop 3
                                                :last-stop 4}))))

(deftest parse-attribute-test
  (testing "Parse attribute record"
    (is (= (parse-attribute "@attribute,1,2") {:attribute-code "attribute"
                                               :first-stop 1
                                               :last-stop 2}))
    (is (= (parse-attribute "@another-attribute,3,4") {:attribute-code "another-attribute"
                                                       :first-stop 3
                                                       :last-stop 4}))))

(deftest parse-start-record-test
  (testing "Parse start record"
    (is (= (parse-start-record ">start    ,0800") {:type :start
                                                   :station :start
                                                   :departure-time "0800"}))
    (is (= (parse-start-record ">begin  ,2415") {:type :start
                                                 :station :begin
                                                 :departure-time "2415"}))))

(deftest parse-final-record-test
  (testing "Parse final record"
    (is (= (parse-final-record "<end,0800") {:type :final
                                             :station :end
                                             :arrival-time "0800"}))
    (is (= (parse-final-record "<finish,2415") {:type :final
                                                :station :finish
                                                :arrival-time "2415"}))))

(deftest parse-platform-record-test
  (testing "Parse platform record"
    (is (= (parse-platform-record "?1   ,2   ,100") {:type :platform
                                                     :arrival-platform "1"
                                                     :departure-platform "2"
                                                     :footnote 100}))
    (is (= (parse-platform-record "?3a   ,4b  ,1002") {:type :platform
                                                       :arrival-platform "3a"
                                                       :departure-platform "4b"
                                                       :footnote 1002}))))
(deftest parse-interval-record-test
  (testing "Parse interval record"
    (is (= (parse-interval-record "+bkg   ,1915    ,1916")
           {:type :interval
            :station :bkg
            :arrival-time "1915"
            :departure-time "1916"}))
    (is (= (parse-interval-record "+asd,   1200,   1225")
           {:type :interval
            :station :asd
            :arrival-time "1200"
            :departure-time "1225"}))))

(deftest parse-passing-record-test
  (testing "Parse passing record"
    (is (= (parse-passing-record ";station1") {:type :passing
                                               :station :station1}))
    (is (= (parse-passing-record ";station2") {:type :passing
                                               :station :station2}))))

(deftest parse-continuation-record-test
  (testing "Parse continuation record"
    (is (= (parse-continuation-record ".station1,0800") {:type :continuation
                                                         :station :station1
                                                         :time "0800"}))
    (is (= (parse-continuation-record ".station2,2415") {:type :continuation
                                                         :station :station2
                                                         :time "2415"}))))

(deftest parse-line-test
  (testing "Parse various line types"
    (is (= (parse-line ">start    ,0800") {:type :stop :data {:type :start
                                       :station :start
                                       :departure-time "0800"}}))
    (is (= (parse-line "<end  ,0800") {:type :stop :data {:type :final
                                     :station :end
                                     :arrival-time "0800"}}))
    (is (= (parse-line "?1b,2,100") {:type :platform :data  {:type :platform
                                    :arrival-platform "1b"
                                    :departure-platform "2"
                                    :footnote 100}}))
    (is (= (parse-line "+bkg,1915,1916") {:type :stop :data {:type :interval
                                          :station :bkg
                                          :arrival-time "1915"
                                          :departure-time "1916"}}))
    (is (= (parse-line ";station1") {:type :stop :data {:type :passing
                                     :station :station1}}))
    (is (= (parse-line ".station1,0800") {:type :stop :data {:type :continuation
                                          :station :station1
                                          :time "0800"}}))
    (is (= (parse-line "@100,01012020,31122020") {:type :delivery :data {:company-number 100
                                     :start-date (jt/local-date "2020-01-01")
                                     :end-date (jt/local-date "2020-12-31")}}))
    (is (= (parse-line "adsfasdf") {:type :unknown, :raw "adsfasdf"}))))

(deftest build-journey-edn-test 
  (testing "test build journey"
    (is (= (build-journey-edn []) {:service-id nil, :type nil, :attributes [], :stops [] :journey-id nil, :footnote nil }))
    (is (= (build-journey-edn [">start    ,0800" "<end    ,0900"])  {:service-id nil, :type nil, :attributes [], :journey-id nil, :footnote nil ,
                                                                        :stops [
                                                                        {:station :start, :arrival-time nil, :departure-time "0800", :arrival-platform nil, :departure-platform nil, :stop-type :start} 
                                                                        {:station :end, :arrival-time "0900", :departure-time nil, :arrival-platform nil, :departure-platform nil, :stop-type :final}]}))
    (is (= (build-journey-edn
            ["#00000002" "%100,03557,      ,001,019," "-00001,000,999" "&IC  ,001,013" ">ekz    ,2339" "?1    ,1    ,00001"
            ".bkf    ,2343" "?1    ,1    ,00001" "+bkg    ,2346,2347" "?2    ,2    ,00001" ";brd"
            "<asd    ,2446" "?4a   ,4a   ,00001"])
           {:service-id 2, 
            :journey-id 3557, 
            :type :ic,
            :footnote "00001",
            :attributes [],
            :stops [{:station :ekz, :arrival-time nil, :departure-time "2339", :arrival-platform "1", :departure-platform "1", :stop-type :start}
                    {:station :bkf, :arrival-time "2343", :departure-time "2343", :arrival-platform "1", :departure-platform "1", :stop-type :continuation}
                    {:station :bkg, :arrival-time "2346", :departure-time "2347", :arrival-platform "2", :departure-platform "2", :stop-type :interval} 
                    {:station :asd, :arrival-time "2446", :departure-time nil, :arrival-platform "4a", :departure-platform "4a", :stop-type :final}]}))))


(deftest process-lines-with-company-filter-test
  (testing "Process lines with company filter"
    (let [lines ["@100,21072025,13122025,0107,IFF Standaard uit RIF" "#00000001" "%100,03557,      ,001,019," "-00001,000,999" "&IC  ,001,013"
                 ">ekz    ,2339" "?1    ,1    ,00001" ".bkf    ,2343"
                 "?1    ,1    ,00001" "+bkg    ,2346,2347" "?2    ,2    ,00001"
                 ";brd" ";gn" "<asd    ,2446" "?4a   ,4a   ,00001"
                 "#00000002" "%101,007,      ,001,019," "-00002,000,999" "&IC  ,001,013"
                 ">ekz    ,2339" "?1    ,1    ,00001" ".bkf    ,2343"
                 "?1    ,1    ,00001" "+bkg    ,2346,2347" "?2    ,2    ,00001"
                 ";brd" "<asd    ,2446" "?4a   ,4a   ,00001"
                 "#00000003" "%100,03558,      ,001,019," "-00003,000,999" "&SPR  ,001,013"
                 ">ekz    ,2339" "?1    ,1    ,00001" ".bkf    ,2343"
                 "?1    ,1    ,00001" ";gn" ";gd" "+bkg    ,2346,2347" "?2    ,2    ,00001"
                 ";brd" "<asd    ,2446" "?4a   ,4a   ,00001"]
          company-number 100]
      (is (= (process-lines-with-company-filter lines company-number)
             [{:service-id 1
               :journey-id 3557
               :type :ic
               :footnote "00001"
               :attributes [],
               :stops [{:station :ekz, :arrival-time nil, :departure-time "2339", :arrival-platform "1", :departure-platform "1", :stop-type :start}
                       {:station :bkf, :arrival-time "2343", :departure-time "2343", :arrival-platform "1", :departure-platform "1", :stop-type :continuation}
                       {:station :bkg, :arrival-time "2346", :departure-time "2347", :arrival-platform "2", :departure-platform "2", :stop-type :interval}
                       {:station :asd, :arrival-time "2446", :departure-time nil, :arrival-platform "4a", :departure-platform "4a", :stop-type :final}]}
              {:service-id 3
               :journey-id 3558
               :type :spr
               :footnote "00003",
               :attributes []
               :stops [{:station :ekz, :arrival-time nil, :departure-time "2339", :arrival-platform "1", :departure-platform "1", :stop-type :start}
                       {:station :bkf, :arrival-time "2343", :departure-time "2343", :arrival-platform "1", :departure-platform "1", :stop-type :continuation}
                       {:station :bkg, :arrival-time "2346", :departure-time "2347", :arrival-platform "2", :departure-platform "2", :stop-type :interval}
                       {:station :asd, :arrival-time "2446", :departure-time nil, :arrival-platform "4a", :departure-platform "4a", :stop-type :final}]}])))))