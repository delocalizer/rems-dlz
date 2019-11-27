(ns rems.application.test-workflow2
  (:require [clojure.test :refer :all]
            [rems.application.workflow2 :as workflow2]
            [rems.permissions :as permissions]))

(deftest test-calculate-permissions
  (testing "commenter may comment only once"
    (let [requested (reduce workflow2/calculate-permissions nil [{:event/type :application.event/created
                                                                  :event/actor "applicant"}
                                                                 {:event/type :application.event/submitted
                                                                  :event/actor "applicant"}
                                                                 {:event/type :application.event/comment-requested
                                                                  :event/actor "handler"
                                                                  :application/commenters ["commenter1" "commenter2"]}])
          commented (reduce workflow2/calculate-permissions requested [{:event/type :application.event/commented
                                                                        :event/actor "commenter1"}])]
      (is (= #{:see-everything :application.command/comment :application.command/remark}
             (permissions/user-permissions requested "commenter1")))
      (is (= #{:see-everything :application.command/remark}
             (permissions/user-permissions commented "commenter1")))
      (is (= #{:see-everything :application.command/comment :application.command/remark}
             (permissions/user-permissions commented "commenter2")))))

  (testing "decider may decide only once"
    ;; TODO: this test doesn't apply to this workflow
    (let [requested (reduce workflow2/calculate-permissions nil [{:event/type :application.event/created
                                                                  :event/actor "applicant"}
                                                                 {:event/type :application.event/submitted
                                                                  :event/actor "applicant"}
                                                                 {:event/type :application.event/decision-requested
                                                                  :event/actor "handler"
                                                                  :application/deciders ["decider"]}])
          decided (reduce workflow2/calculate-permissions requested [{:event/type :application.event/decided
                                                                      :event/actor "decider"}])]
      (is (= #{:see-everything :application.command/approve :application.command/reject :application.command/remark}
             (permissions/user-permissions requested "decider")))
      (is (= #{:see-everything :application.command/remark}
             (permissions/user-permissions decided "decider")))))

  (testing "everyone can accept invitation"
    (let [created (reduce workflow2/calculate-permissions nil [{:event/type :application.event/created
                                                                :event/actor "applicant"}])]
      (is (contains? (permissions/user-permissions created "joe")
                     :application.command/accept-invitation))))

  (testing "nobody can accept invitation for closed application"
    (let [closed (reduce workflow2/calculate-permissions nil [{:event/type :application.event/created
                                                               :event/actor "applicant"}
                                                              {:event/type :application.event/closed
                                                               :event/actor "applicant"}])]
      (is (not (contains? (permissions/user-permissions closed "joe")
                          :application.command/accept-invitation)))
      (is (not (contains? (permissions/user-permissions closed "applicant")
                          :application.command/accept-invitation))))))
