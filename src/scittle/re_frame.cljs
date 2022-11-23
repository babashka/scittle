(ns scittle.re-frame
  (:require
   [sci.configs.re-frame.re-frame :as rf]
   [scittle.core :as scittle]))

(scittle/register-plugin!
 ::re-frame
 rf/config)
