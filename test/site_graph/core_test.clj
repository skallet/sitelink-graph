(ns site-graph.core-test
  (:require [clojure.test :refer :all]
            [site-graph.core :refer :all]))

(def nil-domains ["" "empty domain" "some prefix https://google.com" nil])

(deftest ->domain-kw-test
  (testing "String to URL keyword"
    (testing "Invalid input"
      (doseq [d nil-domains]
        (is (nil? (->domain-kw d)))))
    (testing "Valid input"
      (is (= :google.com (->domain-kw "https://www.google.com/")))
      (is (= :google.com (->domain-kw "http://GoOgLe.com")))
      (is (= :google.com (->domain-kw "https://google.com/path/to/index.php?a=x&b=y"))))))

(deftest keep-nodes-test
  (testing "Valid link subset"
    (is (= #{:google.com :facebook.com}
           (:links (keep-nodes #{:google.com :facebook.com :gmail.com}
                               {:links [:yahoo.com :google.com :facebook.com]}))))))

(defn mock-get [url]
  {:body "This is mocked html https://google.com also contains https://www.facebook.com"})

(deftest xsites-pipe
  (testing "Link process pipe test"
    (with-redefs-fn {#'clj-http.client/get mock-get}
      #(let [sites (transduce xsites conj [] ["http://yahoo.com"])]
         (is (= 1 (count sites)))
         (is (= 2 (count (:links (first sites)))))))))

(deftest xvertices-fn-pipe
  (testing "Creates edges for sites"
    (let [nodes #{:a :b :c}
          sites [{:site-kw :a
                  :links #{:c}}
                 {:site-kw :b
                  :links #{:a}}
                 {:site-kw :c
                  :links #{:a :b}}]
          edges (transduce (xvertices-fn nodes) conj [] sites)]
      (is (= 4 (count edges))))))

(deftest split-edges-test
  (testing "Split edges into directional and undirectional"
    (let [edges [[:a :b]
                 [:a :c]
                 [:b :a]
                 [:c :b]]
          {:keys [directed undirected]} (split-edges edges)]
      (is (= 2 (count directed)))
      (is (= 1 (count undirected))))))
