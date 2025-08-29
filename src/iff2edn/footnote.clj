(ns iff2edn.footnote
  (:require [iff2edn.util :as util]
            [java-time :as jt]))


(defn string-date-vector [s start-date]
  (disj (set (mapv (fn [digit index]
            (case digit
              \1 (jt/plus start-date (jt/days index))
              \0 nil
              (throw (IllegalArgumentException. 
                      (str "Invalid character '" digit "' in binary string. Only '0' and '1' are allowed.")))))
          s
          (range))) nil))

(defn convert-footnotes [footnotes start-date]
  (loop [lines (drop 1 footnotes)
         result {}]
    (if (empty? lines)
      result
      (let [key (subs (first lines) 1)
            string (second lines)
            ;;_ (println "Processing key:" key string)
            vector (string-date-vector string start-date)]
        (recur (drop 2 lines) (assoc result key vector))))))


(comment
 (def footn (convert-footnotes (util/read-lines-to-vector "./resources/footnote.dat") 
                 (jt/local-date "2025-08-25") ))
  (sort (map count (vals footn)))
  (count (get footn "00000" ))
)

