<script lang="ts">
import verificationApi from '@/api/verificationapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { VerifyConfiguration } from '@/entities/task/taskverify';
import type { EvalField } from '@/entities/task/verify/evalfield';
import { fromRuleConfig } from '@/entities/task/verify/jsconfig';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        verifyConfig: {
            type: Object as PropType<VerifyConfiguration>,
            required: true
        }
    },
    setup(props) {
        let config = fromRuleConfig(props.verifyConfig.ruleConfig)
        return reactive({
            config,
            sampleInput: "",
            verifying: false,
            result: {
                success: undefined as boolean | undefined,
                message: ''
            },
            fieldValues: props.verifyConfig.fields.map(() => ({
                value: "",
                type: "Text"
            })) as EvalField[],
            typeOptions: [
                { label: "Number", value: "Number" },
                { label: "Boolean", value: "Boolean" },
                { label: "DateTime", value: "DateTime" },
                { label: "Text", value: "Text" }
            ]
        })
    },
    methods: {
        prepareTest() {
            if (!this.verifying) {
                this.verifying = true
                this.fieldValues = this.verifyConfig.fields.map(() => ({
                    value: "",
                    type: "Text"
                }))
            } else {
                this.verifying = false
                this.fieldValues = []
            }
        },
        evaluate() {
            let fv = {} as { [key: string]: EvalField }
            for (let i in this.verifyConfig.fields) {
                fv[this.verifyConfig.fields[i]] = this.fieldValues[i]
            }
            verificationApi.verifyPythonScript(identityStore().currentIdentity(), {
                code: this.config.snippet,
                fieldValues: fv
            }).then(rsp => {
                console.log("ok: ", rsp.data.result)
            }).catch(err => {
                eventBus.send(EVENT_REMOTE_API_ERROR, err)
            })
        }
    }
})
</script>

<template>
    <n-input type="textarea" v-model:value="config.snippet" placeholder="example:
                result=row[u'col1'] + row[u'col2'] < row[u'col3']"></n-input>
    <n-grid cols="2" v-if="verifying">
        <n-gi v-for="(field, i) in verifyConfig.fields" :key="i">
            <n-input-group>
                <n-tag type="info">{{ field }}</n-tag>
                <n-input v-model:value="fieldValues[i].value" type="text" />
                <n-select v-model:value="fieldValues[i].type" :options="typeOptions"></n-select>
            </n-input-group>
        </n-gi>
    </n-grid>
    <n-input-group>
        <n-button type="default" @click="prepareTest">Test</n-button>
        <n-button type="default" @click="evaluate" v-if="verifying">Evaluate</n-button>
    </n-input-group>
</template>

<style scoped></style>
