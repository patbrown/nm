(ns baby.pat.nm.employ-mm
  (:require [baby.pat.nm.utils :as nmu]
            [baby.pat.jes.vt :as vt]
            [baby.pat.jes.vt.util :as u]
            #?(:clj [babashka.fs :as fs])
            [clojure.spec.alpha :as s]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]))

(defn-spec atoms-employ-wrapper ::vt/vec-of-2
  "Employ function that returns the value of a path in the map"
  [m ::vt/atom-map-or-str path ::vt/qkw-or-vec]
  (let [safe-m (cond
                 (map? m) m
                 (u/atom? m) @m
                 (and (string? m) #?(:clj (fs/exists? m)
                                     :cljs false))
                 #?(:clj (clojure.edn/read-string (slurp m)) :cljs nil)
                 :else m)
        safe-path (nmu/as-ident-path path)]
    [safe-m safe-path]))

(defmulti *employ (fn [variant & args] variant))
(defmethod *employ :default [_ m path]
  (let [[safe-m safe-path] (atoms-employ-wrapper m path)]
    (get-in safe-m (nmu/follow-path safe-m safe-path))))

(defn-spec return-as ::vt/any
  [variant ::vt/qkw pred ::vt/fn return-fn ::vt/fn-or-kw m ::vt/map thing ::vt/any]
  (if (pred thing)
    thing
    (return-fn (*employ variant m thing))))

(defn-spec return-as-entity ::vt/map
  "If given a path, return the map entry, ortherwise return the map. A variant can be provided first to use a named method of employ."
  ([m ::vt/map thing ::vt/map-or-vec] (return-as-entity :default m thing))
  ([variant ::vt/kw m ::vt/map thing ::vt/map-or-vec]
   (return-as variant map? identity m thing)))

(defn-spec return-as-function ::vt/fn
  "Returns  either the function given or the function stored at the path. Notice :function/function is "
  ([m ::vt/map thing ::vt/fn-or-vec] (return-as-function :default m thing))
  ([variant ::vt/kw m ::vt/map thing ::vt/fn-or-vec]
   (return-as variant fn? :function/function m thing)))

(defmethod *employ :follow [_ m path]
  (let [[safe-m safe-path] (atoms-employ-wrapper m path)]
    (clojure.walk/postwalk #(cond
                              (s/valid? ::vt/ident %) (get-in safe-m %)
                              (s/valid? ::vt/idents %) (map (get-in safe-m %))
                              :else %)
                           (*employ :default safe-m safe-path))))

(defmethod *employ :merge [_ m base overlay]
  (let [base (return-as-entity m base)
        overlay (return-as-entity m overlay)]
    (merge base overlay)))

(defmethod *employ :xform [_ m entity xform]
  (let [subject (return-as-entity m entity)
        xform (return-as-function m xform)])
  (xform entity))

(defmethod *employ :follow-merge [_ m base overlay]
  (let [base (return-as-entity :follow m base)
        overlay (return-as-entity :follow m overlay)]
    (merge base overlay)))

(defmethod *employ :follow-xform [_ m subject xform]
  (let [subject (return-as-entity :follow m subject)
        xform (return-as-function :follow m xform)])
  (xform subject))

(defn-spec employ ::vt/any
  ([m ::vt/atom-map-or-str path ::vt/atom-kw-str-or-vec]
   (apply *employ [:default m path]))
  ([variant ::vt/kw m ::vt/atom-map-or-str path ::vt/atom-kw-str-or-vec]
   (apply *employ [variant m path]))
  ([variant ::vt/kw m ::vt/atom-map-or-str path ::vt/atom-kw-str-or-vec & args ::vt/any]
   (apply *employ (into [variant m path] (vec args)))))

(defn-spec <- ::vt/any [m ::vt/atom-map-or-str path ::vt/qkw-or-vec] (employ :default m path))

(defn-spec <-merge ::vt/map
  [m ::vt/atom-map-or-str base ::vt/map-or-vec overlay ::vt/map-or-vec]
  (employ :merge m base overlay))
(defn-spec <-xform ::vt/any
  [m ::vt/atom-map-or-str entity ::vt/map-or-vec xform ::vt/fn-or-vec]
  (employ :xform m entity xform))

(defn-spec <<- ::vt/any
  [m ::vt/atom-map-or-str path ::vt/qkw-or-vec]
  (employ :follow m path))

(defn-spec <<-merge ::vt/map
  [m ::vt/atom-map-or-str base ::vt/map-or-vec overlay ::vt/map-or-vec]
  (employ :follow-merge m base overlay))

(defn-spec <<-xform ::vt/any
  [m ::vt/atom-map-or-str entity ::vt/map-or-vec xform ::vt/fn-or-vec]
  (employ :follow-clone m entity xform))

(defn-spec <-default-value ::vt/any
  "Returns the default position's value for idents and ids."
  [m ::vt/atom-map-or-str id ::vt/ident-or-qkw]
  (<- m (nmu/as-default-value-path id)))

(defn-spec call-ident-fn ::vt/any
  "Calls a function located at the ident."
  [m ::vt/atom-map-or-str ident ::vt/ident-or-qkw]
  (let [f (<-default-value m ident)]
    (f)))

(defn-spec apply-ident-fn ::vt/any
  "Applies the function located at the ident to the supplied args"
  [m ::vt/atom-map-or-str ident ::vt/ident-or-qkw args ::vt/vec]
  (let [f (<-default-value m ident)]
   (apply f args)))

(comment
  (*employ :overlay (atom {:a/id {"sex" {:a/id "sex" :a/a "nuts"}}})
           [:a/id "sex"]
           {:a/a "lips" :a/b 999})
  {:a/a "nuts", :a/b 999, :a/id "sex"}
  {:a/id "sex", :a/a "lips", :a/b 999}
)
;; END EMPLOY
