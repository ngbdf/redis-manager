import { isEmpty } from '@/utils/validate.js'
import { Message } from 'element-ui'

function messageAlert (message, type) {
    let option = {
        showClose: true,
        message: message,
        offset: 60
    }
    if (!isEmpty(type)) {
        option.type = type
    }
    if (type === 'success') {
        Message.success(option)
    } else if (type === 'warning') {
        Message.warning(option)
    } else if (type === 'error') {
        Message.error(option)
    } else {
        Message.info(option)
    }
}

export default {
    info: function (message) {
        return messageAlert(message, null)
    },
    success: function (message) {
        return messageAlert(message, 'success')
    },
    warning: function (message) {
        return messageAlert(message, 'warning')
    },
    error: function (message) {
        return messageAlert(message, 'error')
    }
}
