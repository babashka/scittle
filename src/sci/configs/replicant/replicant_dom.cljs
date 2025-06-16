(ns sci.configs.replicant.replicant-dom
  (:require [replicant.dom :as rd]
            [sci.core :as sci]))

(def rdns (sci/create-ns 'replicant.dom nil))

(def replicant-dom-namespace
  {'render (sci/copy-var rd/render rdns)
   'unmount (sci/copy-var rd/unmount rdns)
   'set-dispatch! (sci/copy-var rd/set-dispatch! rdns)})

(def namespaces {'replicant.dom replicant-dom-namespace})

(def config {:namespaces namespaces})

