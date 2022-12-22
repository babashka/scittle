(ns scittle.pprint
  (:require
   [sci.configs.cljs.pprint :refer [config]]
   [scittle.core :as scittle]))

(prn :config config)

(scittle/register-plugin!
  ::pprint
  config)
