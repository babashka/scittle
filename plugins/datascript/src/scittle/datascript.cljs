(ns scittle.datascript
  {:no-doc true}
  (:require [sci.configs.tonsky.datascript :refer [config]]
            [scittle.core :as scittle]))

(defn init []
  (scittle/register-plugin!
   ::datascript
   config))
