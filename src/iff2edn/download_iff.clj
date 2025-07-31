(ns iff2edn.download-iff
  (:require [hato.client :as http]
            [java-time :as jt]
            [clojure.java.io :as io])
   (:import [java.util.zip ZipInputStream]))

(defn download-and-save-file 
  "Downloads a file from URL and saves it to the given path."
  [url file-path]
  (with-open [in (:body (http/get url {:as :stream}))
              out (io/output-stream file-path)]
    (io/copy in out)))

(defn unzip
  "Unzips a zip file to the given destination directory."
  [zip-file dest-dir]
  (with-open [zip-in (ZipInputStream. (io/input-stream zip-file))]
    (loop []
      (when-let [entry (.getNextEntry zip-in)]
        (let [file (io/file dest-dir (.getName entry))]
          (if (.isDirectory entry)
            (.mkdirs file)
            (do
              (.mkdirs (.getParentFile file))
              (with-open [out (io/output-stream file)]
                (io/copy zip-in out)))))
        (.closeEntry zip-in)
        (recur)))))


(defn get-latest-iff
  "Downloads the latest IFF file from the URL and saves it to the specified path."
  []
  (let [url "https://data.ndovloket.nl/ns/ns-latest.zip"
        today (jt/local-date)
        file-path (str "./resources/" today "-iff.zip") ]
    (println "Downloading latest IFF file from" url "to" file-path)
    (download-and-save-file url file-path)
    (unzip file-path (str "./resources/"))))


;;(get-latest-iff)