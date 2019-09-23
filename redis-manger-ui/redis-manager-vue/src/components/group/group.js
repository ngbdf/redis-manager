import { store } from "@/vuex/store.js";
import { formatTime } from "@/utils/time.js";
import API from "@/api/api.js";

export function getGroupList() {
    let userId = store.getters.getUserId;
      let url = "/group/getGroupList/" + userId;
      API.get(
        url,
        null,
        response => {
          if (response.data.code == 0) {
            let groupList = response.data.data;
            groupList.forEach(group => {
              group.updateTime = formatTime(group.updateTime);
            });
            store.dispatch("setGroupList", groupList);
          } else {
            console.log("No data");
          }
        },
        err => {
          console.log(err);
        }
      );
}