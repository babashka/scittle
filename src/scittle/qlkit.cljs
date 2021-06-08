(ns scittle.qlkit
  (:require [qlkit.core :as ql]
            [sci.core :as sci]
            [scittle.core :as scittle]))

(def qns (sci/create-ns 'qlkit.core nil))

;;directly copy-pasted from sourcecode of qlkit.core
(defn ^:macro defcomponent* [form env nam & bodies]
     (doseq [[nam] bodies]
       (when-not ('#{state query render component-did-mount component-will-unmount component-will-receive-props} nam)
         (throw (ex-info (str "Unknown component member " nam) {}))))
     `(let [key# (keyword ~(str (:name (:ns env))) ~(name nam))] ;;env is {} in scittle, thus (:ns env) is nil, thus component names are not namespaced names
        (def ~nam key#)
        (#'qlkit.core/add-class key#
                     ~(into {:display-name (name nam)}
                            (for [[nam & more :as body] bodies]
                              (if ('#{state query} nam)
                                [(keyword nam)
                                 (last more)]
                                [(keyword nam)
                                 `(fn ~(first more)
                                    ~@(rest more))]))))))

(def qlkit-namespace
  {'add-class (sci/copy-var ql/add-class qns)
   'defcomponent* (sci/copy-var defcomponent* qns)
   'transact!* (sci/copy-var ql/transact!* qns)
   'update-state!* (sci/copy-var ql/update-state!* qns)
   'create-instance (sci/copy-var ql/create-instance qns)
   'get-query (sci/copy-var ql/get-query qns)
   'parse-children (sci/copy-var ql/parse-children qns)
   'parse-children-remote (sci/copy-var ql/parse-children-remote qns)
   'parse-children-sync (sci/copy-var ql/parse-children-sync qns)
   'mount (sci/copy-var ql/mount qns)})

(scittle/register-plugin!
  ::qlkit
  {:namespaces {'qlkit.core qlkit-namespace}})
