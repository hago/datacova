<script lang="ts">
import verificationApi from '@/api/verificationapi';
import type { RuleConfig } from '@/entities/task/taskverify';
import { fromRuleConfig } from '@/entities/task/verify/regexruleconfig';
import { identityStore } from '@/stores/identitystore';
import { sample } from 'lodash';
import { defineComponent, reactive, type PropType } from 'vue';

let changeTime = -1
let timerId = undefined as number | undefined

export default defineComponent({
    props: {
        ruleConfig: {
            type: Object as PropType<RuleConfig>,
            required: true
        }
    },
    setup(props) {
        let regexConfig = fromRuleConfig(props.ruleConfig)
        return reactive({
            regexConfig,
            sampleText: "",
            verifying: false,
            result: {
                success: undefined as boolean | undefined,
                message: ''
            }
        })
    },
    methods: {
        onInput() {
            console.log('verify')
            clearTimeout(timerId)
            if ((!this.verifying) || (this.regexConfig.pattern.trim() === '') || (this.sampleText.trim() === '')) {
                return
            }
            timerId = setTimeout(this.verifyRegex, 2000)
        },
        verifyRegex() {
            verificationApi.verifyRegexRule(identityStore().currentIdentity(), {
                regexConfig: this.regexConfig,
                text: this.sampleText
            }).then(rsp => {
                if (rsp.code === -1) {
                    this.setResult(false, 'Pattern Error')
                } else if (rsp.code === -2) {
                    this.setResult(false, 'Not match')
                } else {
                    this.setResult(true, 'success')
                }
            }).catch(err => {
                this.setResult(false, err)
            })
        },
        setResult(b: boolean, t: string) {
            this.result.success = b
            this.result.message = t
        }
    }
})
</script>

<template>
    <n-checkbox v-model:checked="regexConfig.caseSensitive">Case Sensitive</n-checkbox>
    <n-input type="textarea" v-model:value="regexConfig.pattern" class="marginbelow" placeholder="Regex here"
        @input="onInput()"></n-input>
    <n-input type="textarea" v-model:value="sampleText" class="marginbelow" placeholder="sample text" v-if="verifying"
        @input="onInput()"></n-input>
    <span>
        <n-button default v-if="result.success !== undefined" :class="result.success ? 'match' : 'notmatch'">
            {{ result.message }}
        </n-button>
        <n-button primary @click="verifying = !verifying" class="rightbutton">Verify Regex</n-button>
    </span>
</template>

<style scoped>
.rightbutton {
    float: right;
}

.match {
    color: green
}

.notmatch {
    color: red;
}

.marginbelow {
    margin-bottom: 5px;
}
</style>
