(ns baby.pat.nm.access
  (:require [baby.pat.nm.employ-mm :as employ-mm]
            [baby.pat.jes.vt :as vt]
            [clojure.edn]
            [clojure.string]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]))

(defn-spec access ::vt/any
  "Access an atom or map via an instructions map."
  [m ::vt/atom-map-or-str {:keys [action nm path function args] :as instructions} ::vt/map]
  (let [action (employ-mm/<-default-value m action)
        nm (employ-mm/<-default-value m nm)
        _ (tap> {:a action :s nm :path path :function function :args args})]
    (if (nil? path)
      (action nm)
      (if (nil? function)
        (action nm path)
        (if (nil? args)
          (action nm path function)
          (action nm path function args))))))

(defn-spec bang ::vt/any
  "Provides a vector interface to access."
  ([v ::vt/vec]
   (if (every? vector? v)
     (doall (map bang v))
     (let [m (first v)
           action (second v)
           nm (when (> (count v) 2) (nth v 2))
           path (when (> (count v) 3) (nth v 3))
           function (when (> (count v) 4) (nth v 4))
           args (when (> (count v) 5) (nth v 5))
           access-map (medley.core/filter-vals
                       (fn [v] (when (not= nil v) v))
                       {:action action :nm nm :path path :function function :args args})]
       (access m access-map))))
  ;; This allows mapping of `bang`
  ([m ::vt/atom-map-or-str v ::vt/vec] (if (every? vector? v)
                                     (bang (mapv (fn [a-vec] (into [m] a-vec)) v))
                                     (bang (into [m] v)))))

(def ! bang)

(defn-spec access-with ::vt/fn
  "Takes partial instructions and returns a primed function like `access`.
   If can take partial instructions or a map and partial instructions."
  ([partial-instructions ::vt/vec]
   (fn [m & v]
     (let [instructions (if (vector? (first v))
                          (into (into partial-instructions (first v)) (rest v))
                          (into partial-instructions v))]
       (! (into [m] instructions)))))
  ([m ::vt/atom-map-or-str partial-instructions ::vt/vec]
   (fn [& v]
     (let [instructions (if (vector? (first v))
                          (into (into partial-instructions (first v)) (rest v))
                          (into partial-instructions v))]
       (! (into [m] instructions))))))
