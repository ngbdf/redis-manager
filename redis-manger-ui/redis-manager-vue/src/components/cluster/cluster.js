import { store } from "@/vuex/store.js"
import { isEmpty } from "@/utils/validate.js"
import API from "@/api/api.js"
import router from '@/router'
import message from "@/utils/message.js"

export function getClusterById(clusterId, callback) {
    let groupId = store.getters.getCurrentGroup.groupId
    if (isEmpty(groupId)) {
        router.push({ name: "404" })
        return
    }
    let url = "/cluster/getCluster/" + groupId + "/" + clusterId
    API.get(
        url,
        null,
        response => {
            let result = response.data
            if (result.code === 0) {
                callback(result.data)
            } else {
                router.push({ name: "404" })
                message.error("Get clsuter failed")
            }
        },
        err => {
            message.error(err)
        }
    )
}