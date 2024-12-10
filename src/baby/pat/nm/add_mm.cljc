(ns baby.pat.nm.add-mm
  (:require [baby.pat.nm.utils :as nmu]
            [baby.pat.jes.vt :as vt]
            [baby.pat.jes.vt.util :as u]
            [clojure.spec.alpha :as s]
            [medley.core]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]
            [pyramid.core]))

(defn-spec as-entity ::vt/map
  "Turn an entity into an entity."
  [entity ::vt/map]
  (let [iid (u/qid)
        id (ffirst (medley.core/filter-keys #(= "id" (name %)) entity))]
    {:entity/id (keyword "entity" iid)
     :entity/dt [:dt/id (keyword "dt" (-> id namespace))]
     :entity/entity (assoc entity :entity/ident [:entity/id iid])}))

(defmulti *normalize-as (fn [variant & things] variant))
(defmethod *normalize-as :default [_ m items]
  (pyramid.core/add m items))
(defmethod *normalize-as :as-entities [_ m items]
  (pyramid.core/add m (as-entity items)))
(defmethod *normalize-as :add-all-vts [_ m]
  (let [vts (let [vts (set (filter #(= "vt" (namespace %)) (keys (s/registry))))]
              (apply merge (map (fn [s] {s {::vt/id s
                                            ::vt/vt (s/form s)}}) vts)))]
    (assoc m ::vt/id vts)))

(defn-spec add-normalized ::vt/map
  ([m ::vt/map items ::vt/map-or-vec] (add-normalized :default m items))
  ([variant ::vt/kw m ::vt/map items ::vt/map-or-vec]
   (if (vector? items)
     (apply medley.core/deep-merge (map #(add-normalized variant m %) items))
     (if (and (nmu/normalizable? items) (not (nmu/is-normalized? items)))
       (*normalize-as variant m items)
       (medley.core/deep-merge m items)))))

(defn-spec add-normalized-boilerplate ::vt/discard
  [variant ::vt/kw m ::vt/atom-map-or-str items ::vt/atom-map-str-or-vec]
  #?(:clj (let [[is-atom? is-file? safe-m] (nmu/handle-possible-atom-or-file m)
                [_ _ safe-items] (nmu/handle-possible-atom-or-file items)
                new-m (add-normalized variant safe-m safe-items)]
            (nmu/dispatch-possible-atom-or-file is-atom? is-file? m new-m))
     :cljs (let [[is-atom? safe-m] (nmu/handle-possible-atom m)
                 [_ safe-items] (nmu/handle-possible-atom items)
                 new-m (add-normalized variant safe-m safe-items)]
             (nmu/dispatch-possible-atom is-atom? m new-m))))

(defmulti *add (fn [variant & things] variant))
(defmethod *add :default
  [variant m items] (add-normalized-boilerplate variant m items))
(defmethod *add :as-entities
  [variant m items] (add-normalized-boilerplate variant m items))
(defmethod *add :overlay
  [variant m items] (add-normalized-boilerplate variant m items))
(defmethod *add :add-all-vts
  [variant m] (add-normalized-boilerplate variant m nil))

(defn-spec add ::vt/atom-or-map
  "Adds items to m as normalized collections."
  ([m ::vt/atom-map-or-str items ::vt/atom-map-str-or-vec]
   (*add :default m items))
  ([variant ::vt/kw m ::vt/atom-map-or-str items ::vt/atom-map-str-or-vec]
   (*add variant m items)))
