(ns baby.pat.nm.change-mm
  (:require [baby.pat.nm.utils :as nmu]
            [baby.pat.nm.employ-mm :as employ-mm]
            [baby.pat.jes.vt :as vt]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]))

(defn-spec default-change-wrapper ::vt/atom-or-map
  ([variant ::vt/kw m ::vt/atom-or-map path ::vt/vec function ::vt/fn-or-vec]
   #?(:clj (let [[is-atom? is-file? safe-map] (nmu/handle-possible-atom-or-file m)
                 safe-function (employ-mm/return-as-function safe-map function)
                 new-map (update-in safe-map (nmu/follow-path safe-map path) safe-function)]
             (nmu/dispatch-possible-atom-or-file is-atom? is-file? m new-map))
      :cljs (let [[is-atom? safe-map] (nmu/handle-possible-atom m)
                  safe-function (employ-mm/return-as-function safe-map function)
                  new-map (update-in safe-map (nmu/follow-path safe-map path) safe-function)]
              (nmu/dispatch-possible-atom is-atom? m new-map))))
  #?(:clj ([variant ::vt/kw map ::vt/atom-or-map path ::vt/vec function ::vt/fn-or-vec args ::vt/any]
           (let [[is-atom? is-file? safe-map] (nmu/handle-possible-atom-or-file map)
                 safe-function (employ-mm/return-as-function safe-map function)
                 new-map (update-in safe-map (nmu/follow-path safe-map path) safe-function args)]
             (nmu/dispatch-possible-atom-or-file is-atom? is-file? map new-map)))
     :cljs ([variant ::vt/kw map ::vt/atom-or-map path ::vt/vec function ::vt/fn-or-vec args ::vt/any]
            (let [[is-atom? safe-map] (nmu/handle-possible-atom map)
                  safe-function (employ-mm/return-as-function safe-map function)
                  new-map (update-in safe-map (nmu/follow-path safe-map path) safe-function args)]
              (nmu/dispatch-possible-atom is-atom? map new-map)))))

(defmulti *change (fn [variant & args] variant))
(defmethod *change :default
  ([variant m path function]
   (apply default-change-wrapper [variant m path function]))
  ([variant m path function args]
   (apply default-change-wrapper [variant m path function args])))

(defn-spec change ::vt/atom-or-map
  "Takes m, path, and then the args to '(swap! c update-in p ...)' or '(update-in c p ...)'."
  ([m ::vt/atom-map-or-str path ::vt/qkw-or-vec function ::vt/fn-or-vec]
   (*change :default m (nmu/as-ident-path function)))
  ([m ::vt/atom-map-or-str path ::vt/qkw-or-vec function ::vt/fn-or-vec args ::vt/any]
   (*change :default m (nmu/as-ident-path path) function args)))
