(ns baby.pat.nm.ix
  (:require [exoscale.interceptor]
            [baby.pat.jes.chain :as chain]
            [baby.pat.jes.function :as function]
            [baby.pat.nm.employ-mm :refer [<- <-default-value]]
            [baby.pat.nm.add-mm :refer [add]]
            [baby.pat.jes.vt :as vt]
            [orchestra.core #?(:clj :refer :cljs :refer-macros) [defn-spec]]))

(defn-spec hydrate-chain ::vt/vec-of-maps [m ::vt/atom-or-map chain-ident ::vt/ident]
  (let [chain (<-default-value m chain-ident)]
    (mapv #(<-default-value m %) chain)))

(defn-spec execute-chain ::vt/any
  [m ::vt/atom-or-map request ::vt/map chain-ident ::vt/ident]
  (exoscale.interceptor/execute request (hydrate-chain m chain-ident)))
