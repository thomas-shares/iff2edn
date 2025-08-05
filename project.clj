(defproject iff2edn "0.1.0-SNAPSHOT"
  :description "convert IFF to EDN format"
  :url "https://github.com/thomas-shares/iff2edn"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.1"]
                 [clojure.java-time "1.4.3"]
                 [hato "1.0.0"]]
    :plugins [[lein-ancient "0.6.15"]
              [lein-auto "0.1.3"]]
  :jvm-opts ["-Xmx2g"]
  :repl-options {:init-ns iff2edn.core})
