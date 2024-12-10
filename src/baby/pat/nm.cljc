(ns baby.pat.nm
  (:require [baby.pat.nm.access :as access]
            [baby.pat.nm.add-mm :as add-mm]
            [baby.pat.nm.change-mm :as change-mm]
            [baby.pat.nm.employ-mm :as employ-mm]
            [baby.pat.nm.ix :as ix]
            [baby.pat.nm.rm-mm :as rm-mm]
            [baby.pat.nm.utils :as nmu]
            [baby.pat.jes.vt :as vt]
            [baby.pat.jes.vt.util :as u]
            [exoscale.interceptor]))

(def normalizable? "Is this a normalizable map?" nmu/normalizable?)
(def is-normalized? "Is this a normalizable map?" nmu/is-normalized?)
(def follow-path "Follows a path in a normalized map." nmu/follow-path)
(def add "Adds an entity/entities to a normalized map, optional variant method dispatch." add-mm/add)
(def rm "Removes an entity from a normalized map. Optional variants like `:follow` removes all mentions." add-mm/add)
(def change "Changes an entity in a nomalized map. Called like swap! or reset!" change-mm/change)
(def return-as employ-mm/return-as)
(def return-as-entity employ-mm/return-as-entity)
(def return-as-function employ-mm/return-as-function)
(def employ "Uses an entity in a normalized map. Use like get-in variant :follow respects nested maps. Many variants allow for more complex behavior on dispatch." employ-mm/employ)
(def <- "DWIM pulls a path from a nm, does not follow nested idents" employ-mm/<-)
(def <-merge "DWIM pulls a path from a nm and merges another path or map on top of it." employ-mm/<-merge)
(def <-xform "Pulls a path from a nm and applies a function to it." employ-mm/<-xform)
(def <<- "DWIM pulls a path from a nm, follows nested idents." employ-mm/<<-)
(def <<-merge "DWIM pulls a path from a nm and merges another path or map on top of it. Follows nested paths." employ-mm/<<-merge)
(def <<-xform "Pulls a path from a nm and applies a function to it. Follows nested paths." employ-mm/<<-xform)
(def <-default-value "Given an id or an ident pulls the default value." employ-mm/<-default-value)
(def <-call-fn "Calls a function provided." employ-mm/call-ident-fn)
(def <-apply-fn "Applies a function to the provided args." employ-mm/apply-ident-fn)
(def access "Common access point for normalized maps." access/access)
(def bang "Vector interface for normalized map access." access/!)
(def ! "Vector interface for normalized map access." access/!)
(def access-with "Takes partial access instructions and/or optional nm. Returns a functions prepoulated to access nm" access/access-with)
(def hydrate-chain ix/hydrate-chain)
(def execute-chain ix/execute-chain)
(defmulti activate! (fn [m ident] (u/id-kw-for (<- m ident))))
(defmulti deactivate! (fn [m ident] (u/id-kw-for (<- m ident))))
(defmulti toggle! (fn [m ident] (u/id-kw-for (<- m ident))))
