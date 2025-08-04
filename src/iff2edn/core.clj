(ns iff2edn.core
  (:require [iff2edn.util :as util]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [java-time :as jt]))






;; Test the expansion

;; This will create 3 journey instances (one for each valid date)
;; Each with properly calculated arrival/departure dates for all stops
;;(println "Number of expanded journeys:" (count expanded-journeys))
;;(println "First expanded journey:")
;;
;;(clojure.pprint/pprint expanded-journeys)