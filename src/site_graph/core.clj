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

(defn ->domain-kw [link]
  (when (and link (re-matches url-regex link))
    (let [link (-> link
                   (string/replace #"www\." "")
                   (string/lower-case))]
      (keyword (nth (re-find url-regex link) 2)))))

(defn extract-links [{:keys [source] :as data}]
  (assoc data
         :links (into #{}
                      (map (comp ->domain-kw first)
                           (re-seq url-regex source)))))

(defn link-replace [coll link]
  (when (contains? coll link)
    link))

(defn keep-nodes [nodes data]
  (update data
          :links
          (fn [links]
            (->> links
              (map (partial link-replace nodes))
              (filter identity)
              (into #{})))))

(def xsites
  (comp (filter (complement empty?))
        (map string/trim)
        (map #(hash-map :site %
                        :site-kw (->domain-kw %)))
        (filter :site-kw)
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

(defn edge-name [e]
  (string/join "."
    (->> e
         sort
         (map name))))

(defn split-edges [edges]
  (let [grouped-edges (vals (group-by edge-name edges))]
    {:directed (->> grouped-edges
                    (filter #(= (count %) 1))
                    (map first)
                    (map #(conj % {:color :blue})))
     :undirected (->> grouped-edges
                      (filter #(> (count %) 1))
                      (map first)
                      (map #(conj % {:color :red})))}))

(defn create-graph [nodes edges]
  (let [{:keys [directed undirected]} (split-edges edges)]
    (-> (apply uber/graph nodes)
        (uber/add-directed-edges* directed)
        (uber/add-edges* undirected))))

(defn build-graph [source-filename]
  (let [filename (string/replace source-filename #"\.\w+$" "")
        sites (reader/transduce-file xsites conj [] source-filename)
        nodes (into #{} (map :site-kw sites))
        edges (transduce (xvertices-fn nodes) conj [] sites)
        graph (create-graph nodes edges)]
    (when-not (empty? nodes)
      (uber/viz-graph graph {:save {:filename (str filename ".png")
                                    :format :png}}))
    graph))

(comment
  (def g (build-graph "sites.txt"))
  (reader/transduce-file xsites conj [] "sites.txt")
  (split-edges
    (transduce (xvertices-fn #{:a :b :c}) conj [] [{:site-kw :a :links #{:b :c}}
                                                   {:site-kw :b :links #{:a :c}}
                                                   {:site-kw :c :links #{}}])))

(defn -main
  "Open file and create graph, if some URLs are found."
  [& args]
  (if-let [filename (first args)]
    (build-graph filename)
    (prn "Please specify input file")))
