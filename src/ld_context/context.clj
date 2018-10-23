(ns ld-context.context
  (:require [clojure.java.io :as io]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]])
  (:import [org.apache.jena.rdf.model Model ModelFactory Literal Resource]
           [org.apache.jena.ontology OntResource]))

(def to-case csk/->snake_case)

(def context-curies [["id" "@id"]
                     ["type" "@type"]
                     ["http://purl.obolibrary.org/obo/SEPIO_" "SEPIO"]
                     ["http://purl.org/oban/" "OBAN"]
                     ["http://purl.org/dc/terms/" "DC"]
                     ["http://purl.org/pav/" "PAV"]
                     ["http://purl.obolibrary.org/obo/RO_" "RO"]
                     ["http://purl.obolibrary.org/obo/BFO_" "BFO"]
                     ["http://purl.obolibrary.org/obo/OBI_" "OBI"]
                     ["http://purl.obolibrary.org/obo/IAO_", "IAO"]])

;; Inefficient, naievely tests curies against start of string
;; a tree or hash based search could be more optimal.
(defn substitue-curie [s curies]
  (if-let [match (->> curies (filter #(str/starts-with? s (first %))) first)]
    (str/replace-first s (first match) (str (second match) ":"))
    s))

(defn curied-uri [resource curies]
  (when-let [uri (.getURI resource)]
    (substitue-curie uri curies)))

(defn ontology-model [source_files]
  (let [model (ModelFactory/createOntologyModel)]
    (doseq [f source_files]
      (with-open [is (io/input-stream f)]
        (.read model is nil)))
    model))

(defn label [resource]
  (when-let [l (.getLabel resource nil)]
    (to-case l)))

(defn unlabeled? [resource]
  (not (and (.getURI resource) (label resource))))

(defn make-object-property-tuple [resource]
  [(label resource) {"id" (curied-uri resource context-curies), "type" "id"}])

(defn object-properties [model]
  (-> model .listObjectProperties iterator-seq))

(defn context-object-properties [model]
  (->> model object-properties (remove unlabeled?) (map make-object-property-tuple)))

(defn make-data-property-tuple [resource]
  [(label resource) (curied-uri resource context-curies)])

(defn data-properties [model]
  (-> model .listDatatypeProperties iterator-seq))

(defn context-data-properties [model]
  (->> model data-properties (remove unlabeled?) (map make-data-property-tuple)))

(defn context-properties [model]
  (concat (context-object-properties model) (context-data-properties model)))

(defn properties-with-duplicate-labels [property-list]
  (let [label-frequencies (frequencies (map first property-list))
        duplicate-labels (into #{} (filter #(> (val %) 1) label-frequencies))]
    (filter #(duplicate-labels (first %)) property-list)))

(defn construct-context-map [model]
  (let [m (into (ordered-map) (concat context-curies (context-properties model)))]
    {"@context" m}))

(defn write-context [model]
  ())

