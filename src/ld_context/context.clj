(ns ld-context.context
  (:require [clojure.java.io :as io]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]
            [flatland.ordered.map :refer [ordered-map]]
            [cheshire.core :as json])
  (:import [org.apache.jena.rdf.model Model ModelFactory Literal Resource]
           [org.apache.jena.ontology OntResource]))

(def to-case csk/->snake_case)

(def context-curies [["@id" "id"]
                     ["@type" "type"]
                     ["http://purl.obolibrary.org/obo/SEPIO_" "SEPIO"]
                     ["http://purl.org/oban/" "OBAN"]
                     ["http://purl.org/dc/terms/" "DCTERMS"]
                     ["http://purl.org/pav/" "PAV"]
                     ["http://purl.obolibrary.org/obo/RO_" "RO"]
                     ["http://purl.obolibrary.org/obo/BFO_" "BFO"]
                     ["http://purl.obolibrary.org/obo/OBI_" "OBI"]
                     ["http://purl.obolibrary.org/obo/IAO_", "IAO"]
                     ["http://biohackathon.org/resource/faldo#" "FALDO"]
                     ["http://purl.obolibrary.org/obo/GENO_" "GENO"]
                     ["http://purl.org/dc/elements/1.1/" "DC"]])

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
  [(label resource) {"@id" (curied-uri resource context-curies), "@type" "@id"}])

(defn object-properties [model]
  (-> model .listObjectProperties iterator-seq))

(defn context-object-properties [model]
  (->> model object-properties (remove unlabeled?) (map make-object-property-tuple)
       (sort-by first)))

(defn make-data-property-tuple [resource]
  [(label resource) (curied-uri resource context-curies)])

(defn data-properties [model]
  (-> model .listDatatypeProperties iterator-seq))

(defn context-data-properties [model]
  (->> model data-properties (remove unlabeled?) (map make-data-property-tuple)
       (sort-by first)))

(defn context-properties [model]
  (concat (context-object-properties model) (context-data-properties model)))

(defn context-map [property-list]
  (let [cx (map reverse context-curies)
        m (into (ordered-map) (concat cx property-list))]
    {"@context" m}))

(defn write-context [m target-file]
  (with-open [s (io/writer target-file)]
    (json/generate-stream m s {:pretty true})))

(defn properties-with-duplicate-labels [property-list]
  (let [label-frequencies (frequencies (map first property-list))
        duplicate-labels (into #{} (filter #(> (val %) 1) label-frequencies))]
    (filter #(duplicate-labels (first %)) property-list)))

(defn report-duplicates [property-list]
  (when-let [dups (seq (properties-with-duplicate-labels property-list))]
    (println "Duplicate property labels exist:")
    (clojure.pprint/pprint dups)))

(defn write-context-from-owl [output-file input-file-list]
  (let [model (ontology-model input-file-list)
        props (context-properties model)]
    (report-duplicates props)
    (-> props context-map (write-context output-file))))
