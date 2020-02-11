(ns site-graph.core
  (:gen-class)
  (:require [site-graph.reader :as reader]
            [clj-http.client :as client]
            [clojure.string :as string]
            [ubergraph.core :as uber]))

(defn fetch-site [{:keys [site] :as data}]
  (assoc data
         :source (-> (client/get site)
                     :body)))

(def url-regex #"(http|ftp|https)://([\w_-]+(?:(?:\.[\w_-]+)+))([\w.,@?^=%&;:/~+#-]*[\w@?^=%&/~+#-])?")

(defn to-host [link]
  (let [link (string/replace link #"www\." "")]
    (keyword (nth (first (re-seq url-regex link)) 2))))

(defn extract-links [{:keys [source] :as data}]
  (assoc data
         :links (into #{}
                      (map (comp keyword
                                 #(string/replace % #"www\." "")
                                 string/lower-case
                                 #(nth % 2))
                           (re-seq url-regex source)))))

(defn link-eq [a b]
  (= a b))

(defn link-in-subset [s link]
  (not
    (empty?
      (filter (partial link-eq link)
              s))))

(defn link-replace [coll link]
  (first
    (filter (partial link-eq link)
            coll)))

(defn keep-nodes [nodes data]
  (update data
          :links
          (fn [links]
            (->> links
              (map (partial link-replace nodes))
              (filter identity)
              (into #{})))))

(defn create-graph [nodes edges]
  (uber/add-directed-edges* (apply uber/graph nodes)
                            edges))

(def xsites
  (comp (filter (complement empty?))
        (map #(hash-map :site (string/lower-case %)
                        :site-kw (to-host (string/lower-case %))))
        (map fetch-site)
        (map extract-links)))

(defn xvertices-fn [nodes]
  (comp (map (partial keep-nodes nodes))
        (mapcat (fn [{:keys [site-kw links]}]
                  (let [h site-kw]
                    (map (fn [link] [h link])
                         links))))
        (filter (fn [[a b]]
                  (not= a b)))))

(defn build-graph [source-filename]
  (let [sites (reader/transduce-file xsites conj [] source-filename)
        nodes (map :site-kw sites)
        edges (transduce (xvertices-fn nodes) conj [] sites)
        graph (create-graph nodes edges)]
    (uber/viz-graph graph {:save {:filename (str source-filename ".png")
                                  :format :png}})
    graph))

(comment
  (def g (build-graph "sites"))
  (reader/transduce-file xsites conj [] "sites"))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
