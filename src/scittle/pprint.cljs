(ns scittle.pprint
  (:require
   [sci.configs.cljs.pprint :refer [config]]
   [scittle.core :as scittle]))

(scittle/register-plugin!
  ::pprint
  config)
