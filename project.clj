(defproject site-graph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hickory "0.7.1"]
                 [clj-http "3.10.0"]
                 [ubergraph "0.8.2"]]
  :main ^:skip-aot site-graph.core
  :target-path "target/%s"
  :uberjar-name "sitegraph.jar"
  :profiles
  {:uberjar {:aot :all}
   :dev
   {:dependencies [[proto-repl "0.3.1"]]}})
