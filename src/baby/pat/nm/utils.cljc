(ns baby.pat.nm.utils
  (:require [baby.pat.jes.vt :as vt]
            [baby.pat.jes.vt.util :as u]
            [babashka.fs :as fs]
            [clojure.walk]
            [clojure.spec.alpha :as s]
            [medley.core]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]
            [baby.pat.jes.vt.util :as n]))

(def normalizable? u/normalizable?)
(def is-normalized? u/is-normalized?)
(defn-spec follow-path ::vt/vec
  "Follows normalized entities to return the true value coords of path. Ripped from fulcro.a.n-s"
  [m ::vt/map path ::vt/vec]
  (loop [[h & t] path
         new-path []]
    (if h
      (let [np (conj new-path h)
            c  (get-in m np)]
        (if (s/valid? ::vt/ident c)
          (recur t c)
          (recur t (conj new-path h))))
      (if (not= path new-path)
        new-path
        path))))

(defn-spec handle-possible-atom ::vt/vec
  "A helpful function to help with atoms.
   Returns [is-atom? safe-thing]"
  [thing ::vt/any]
  (if (instance? #?(:clj clojure.lang.IAtom
                    :cljs cljs.core/Atom) thing)
    [true @thing]
    [false thing]))

(defn-spec dispatch-possible-atom ::vt/discard
  "A wrapper that helps make consistent return values for atoms and maps"
  [is-atom? ::vt/? m ::vt/any new-m ::vt/any]
  (if is-atom?
    (reset! m new-m)
    new-m))

(defn-spec handle-possible-file ::vt/vec
  "A helpful function to help with files as args.
   Returns [is-atom? safe-thing]"
  [thing ::vt/any]
  (if (fs/exists? thing)
    [true (clojure.edn/read-string (slurp thing))]
    [false thing]))

(defn-spec dispatch-possible-file ::vt/discard
  "A wrapper that helps make consistent return values for atoms and maps"
  [is-file? ::vt/? f-nm ::vt/any new-nm ::vt/any]
  (if is-file?
    (let [_ (spit f-nm new-nm)]
      new-nm)
    new-nm))

#?(:clj (defn-spec handle-possible-atom-or-file ::vt/vec
          "A helpful function to help with atoms or files as args.
   Returns [is-atom? is-file? safe-thing]"
          [thing ::vt/any]
          (let [is-file? (fs/exists? thing)
                is-atom? (instance? (u/atom? thing))
                safe-thing (if is-file?
                             (clojure.edn/read-string (slurp thing))
                             (if is-atom?
                               @thing
                               thing))]
            [is-atom? is-file? safe-thing])))

#?(:clj (defn-spec dispatch-possible-atom-or-file ::vt/discard
          "A wrapper that helps make consistent return values for atoms and maps"
          [is-atom? ::vt/? is-file? ::vt/? old-nm ::vt/map new-nm ::vt/map]
          (if is-file?
            (let [_ (spit old-nm new-nm)]
              new-nm
              (if is-atom?
                (reset! old-nm new-nm)))
            new-nm)))

(defn-spec id->id-kw ::vt/qkw
  "Infers the id-kw for a given qkw using it's namespace."
  [id ::vt/qkw]
  (keyword (namespace id) "id"))

(defn-spec id->ident ::vt/ident [id ::vt/qkw]
  [(id->id-kw id) id])

(defn-spec as-ident-path ::vt/vec
  "Takes a thing and if it's a keyword treats it as part of a default ident.
   If it's a vector, treats it as a path already.   "
  [thing ::vt/qkw-or-vec]
  (if (keyword? thing)
    [(id->id-kw thing) thing]
    thing))

(defn-spec as-default-value-path ::vt/vec
  [thing ::vt/qkw-or-vec]
  (let [ident (id->ident (u/id-of thing))
        nmsp (-> ident first namespace)]
    (into ident (keyword nmsp nmsp))))
