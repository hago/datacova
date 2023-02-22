<script lang="ts">
import { EVENT_REMOTE_API_ERROR, EVENT_TASKINFO_CLOSE_RECIPIENTS_EDITOR } from '@/entities/events';
import type { TaskExtra } from '@/entities/task/task';
import { eventBus } from '@/util/eventbus';
import { computed, defineComponent, reactive, type PropType } from 'vue';

interface EmailParseResult {
    emails: string[]
    invalidItems: string[]
}

function joinRecipients(list: string[] | null) {
    return null === list ? '' : list.join(';')
}

function parseEmailString(s: string, delimiter: string = ';'): EmailParseResult {
    let l = s.split(';').map(s => s.trim()).filter(s => s !== '')
    let emails = [] as string[]
    let invalids = [] as string[]
    for (let i of l) {
        if (i.match(/[^@]+@[^@\s]/) === null) {
            invalids.push(i)
        } else {
            emails.push(i)
        }
    }
    return {
        emails: emails,
        invalidItems: invalids
    }
}

export default defineComponent({
    props: {
        extra: {
            type: Object as PropType<TaskExtra>,
            required: true
        }
    },
    setup(props) {
        return reactive({
            toText: joinRecipients(props.extra.mailRecipients),
            ccText: joinRecipients(props.extra.mailCCRecipients),
            bccText: joinRecipients(props.extra.mailBCCRecipients),
            show: true
        })
    },
    methods: {
        cancel() {
            //console.log("send cancel")
            this.show = false
            eventBus.send(EVENT_TASKINFO_CLOSE_RECIPIENTS_EDITOR)
        },
        update() {
            if (this.doneTo() && this.doneCc() && this.doneBcc()) {
                this.cancel()
            }
        },
        doneTo(): boolean {
            let r = parseEmailString(this.toText)
            this.extra.mailRecipients = r.emails
            if (r.invalidItems.length > 0) {
                eventBus.send(EVENT_REMOTE_API_ERROR, `Not valid email addresses: ${r.invalidItems}`)
                return false
            } else {
                return true
            }
        },
        doneCc(): boolean {
            let r = parseEmailString(this.ccText)
            this.extra.mailCCRecipients = r.emails
            if (r.invalidItems.length > 0) {
                eventBus.send(EVENT_REMOTE_API_ERROR, `Not valid email addresses: ${r.invalidItems}`)
                return false
            } else {
                return true
            }
        },
        doneBcc(): boolean {
            let r = parseEmailString(this.bccText)
            this.extra.mailBCCRecipients = r.emails
            if (r.invalidItems.length > 0) {
                eventBus.send(EVENT_REMOTE_API_ERROR, `Not valid email addresses: ${r.invalidItems}`)
                return false
            } else {
                return true
            }
        }
    }
})

</script>

<template>
    <n-modal :show="show">
        <n-card style="width: 600px" title="" size="small" :bordered="false" role="dialog" aria-modal="true">
            <n-card title="To" :bordered="true">
                <n-input v-model:value="toText" type="textarea" @blur="doneTo"></n-input>
            </n-card>
            <n-card title="CC" :bordered="true">
                <n-input :value="ccText" type="textarea" @blur="doneCc"></n-input>
            </n-card>
            <n-card title="BCC" :bordered="true">
                <n-input :value="bccText" type="textarea" @blur="doneBcc"></n-input>
            </n-card>
            <div style="margin-top: 5px;">
                <n-button type="error" style="float: right;" @click="cancel">Cancel</n-button>
                <n-button type="primary" style="float: right; margin-right: 10px;" @click="update">Update</n-button>
            </div>
        </n-card>
    </n-modal>
</template>
