(ns site-graph.reader
  (:require [clojure.java.io :as io]))

(def separator (java.io.File/separator))

(defn check-file [name]
  (let [res-filename (str "resources" separator name)]
    (cond
      (.exists (io/file name)) name
      (.exists (io/file res-filename)) res-filename
      :else nil)))

(defn transduce-file [xcomp f init name]
  (when-let [filename (check-file name)]
    (with-open [rdr (io/reader filename)]
      (transduce
        xcomp
        f
        init
        (line-seq rdr)))))
