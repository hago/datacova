<script lang="ts">
import { EVENT_TASKINFO_CLOSE_RECIPIENTS_EDITOR } from '@/entities/events';
import type { TaskExtra } from '@/entities/task/task';
import { eventBus } from '@/util/eventbus';
import { computed, defineComponent, reactive, type PropType } from 'vue';

function joinRecipients(list: string[] | null) {
    return null === list ? '' : list.join(';')
}

function parseRecipients(s: string): string[] {
    let l = s.split(';').map(s => s.trim())
    
    return l
}

export default defineComponent({
    props: {
        extra: {
            type: Object as PropType<TaskExtra>,
            required: true
        }
    },
    setup(props) {
        const toText = computed({
            get() { return joinRecipients(props.extra.mailRecipients) },
            set(value: string) { props.extra.mailRecipients = parseRecipients(value) }
        })
        const ccText = computed({
            get() { return joinRecipients(props.extra.mailCCRecipients) },
            set(value: string) { props.extra.mailCCRecipients = parseRecipients(value) }
        })
        const bccText = computed({
            get() { return joinRecipients(props.extra.mailBCCRecipients) },
            set(value: string) { props.extra.mailBCCRecipients = parseRecipients(value) }
        })
        return reactive({
            toText,
            ccText,
            bccText,
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

        }
    }
})

</script>

<template>
    <n-modal :show="show">
        <n-card style="width: 600px" title="" size="small" :bordered="false" role="dialog" aria-modal="true">
            <n-card title="To" :bordered="true">
                <n-input v-bind:value="toText" type="textarea"></n-input>
            </n-card>
            <n-card title="CC" :bordered="true">
                <n-input v-bind:value="ccText" type="textarea"></n-input>
            </n-card>
            <n-card title="BCC" :bordered="true">
                <n-input v-bind:value="bccText" type="textarea"></n-input>
            </n-card>
            <div style="margin-top: 5px;">
                <n-button type="error" style="float: right;" @click="cancel">Cancel</n-button>
                <n-button type="primary" style="float: right; margin-right: 10px;" @click="update">Update</n-button>
            </div>
        </n-card>
    </n-modal>
</template>
