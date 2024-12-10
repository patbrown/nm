(ns baby.pat.nm.rm-mm
  (:require [baby.pat.nm.utils :as nmu]
            [baby.pat.jes.vt :as vt]
            [clojure.spec.alpha :as s]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]
            [pyramid.core]))

;; ## RM
(defn-spec remove-normalized ::vt/map
  "Removes a path from a normalized map."
  [m ::vt/map path ::vt/vec]
  (pyramid.core/delete m path))

(defmulti *rm (fn [variant & args] variant))

(defn-spec follow-remove-normalized ::vt/map
  "Removes a path and all it's references from a normalized map."
  [m ::vt/map path ::vt/vec]
  (let [a (atom [path])
        ev (vec (filter vector? (vals (get-in m path))))
        _ (doall (map #(when (s/valid? ::vt/ident-or-idents %)
                        (swap! a conj %)) ev))
        for-removal (mapv vec (partition 2 (flatten @a)))]
    (loop [xs (seq for-removal)
           result m]
      (if xs
        (let [x (first xs)]
          (recur (next xs) (*rm :default result x)))
        result))))

(defmethod *rm :default [_ m path]
  #?(:clj (let [[is-atom? is-file? m] (nmu/handle-possible-atom-or-file m)
                new-m (remove-normalized m path)]
            (nmu/dispatch-possible-atom-or-file is-atom? is-file? m new-m))
     :cljs (let [[is-atom? m] (nmu/handle-possible-atom m)
                 new-m (remove-normalized m path)]
             (nmu/dispatch-possible-atom is-atom? m new-m))))

(defmethod *rm :follow [_ m path]
  #?(:clj (let [[is-atom? is-file? m] (nmu/handle-possible-atom-or-file m)
                new-m (follow-remove-normalized m path)]
            (nmu/dispatch-possible-atom-or-file is-atom? is-file? m new-m))
     :cljs (let [[is-atom? m] (nmu/handle-possible-atom m)
                 new-m (follow-remove-normalized m path)]
             (nmu/dispatch-possible-atom is-atom? m new-m))))

(defn-spec rm ::vt/atom-or-map
  "Removes an ident from m."
  ([m ::vt/atom-map-or-str path ::vt/qkw-or-vec]
   (rm :default m (nmu/as-ident-path path)))
  ([variant ::vt/kw m ::vt/atom-map-or-str path ::vt/qkw-or-vec]
   (*rm variant m (nmu/as-ident-path path))))

