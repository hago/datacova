<script lang="ts">
import verificationApi from '@/api/verificationapi';
import type DistFtpConfiguration from '@/entities/task/dist/distconfigftp';
import { newDistFtpConfiguration } from '@/entities/task/dist/distconfigftp';
import type { Task } from '@/entities/task/task';
import type { TaskActionDistribute } from '@/entities/task/taskdist';
import { identityStore } from '@/stores/identitystore';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        action: {
            type: Object as PropType<TaskActionDistribute>,
            required: true
        },
        task: {
            type: Object as PropType<Task>,
            required: true
        },
        readonly: {
            type: Boolean,
            required: true
        }
    },
    setup(props) {
        let conf = newDistFtpConfiguration(props.action.configuration)
        return reactive({
            conf,
            ftpMessage: {
                success: false,
                text: ""
            },
            ftpmsgstyle: "ftpsuccess"
        })
    },
    methods: {
        verifyFtp() {
            if (!this.ftpParamsCheck(this.conf)) {
                return
            }
            let user = identityStore().currentIdentity()
            verificationApi.verifyFtp(user, this.conf).then(rsp => {
                this.showMessage(true, `Ftp working directory is ${rsp.data.pwd}`)
            }).catch(err => {
                this.showMessage(false, err)
            })
        },
        showMessage(r: boolean, t: string) {
            this.ftpMessage = {
                success: r,
                text: t
            }
            this.ftpmsgstyle = r ? "ftpsuccess" : "ftpfail"
        },
        ftpParamsCheck(config: DistFtpConfiguration): boolean {
            if ((config.host === undefined) || (config.host === null) || (config.host.trim() === "")) {
                (this.$refs["host"] as HTMLInputElement).focus()
                this.showMessage(false, "Host information missing")
                return false
            }
            if ((config.port === undefined) || (config.port === null) || (config.port <= 0) || (config.port > 65535)) {
                (this.$refs["port"] as HTMLInputElement).focus()
                this.showMessage(false, "Port information missing")
                return false
            }
            if ((config.login === undefined) || (config.login === null) || (config.login.trim() === "")) {
                (this.$refs["login"] as HTMLInputElement).focus()
                this.showMessage(false, "Login name missing")
                return false
            }
            if ((config.password === undefined) || (config.password === null)) {
                (this.$refs["password"] as HTMLInputElement).focus()
                this.showMessage(false, "password missing")
                return false
            }
            return true
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span>Host</span>
            <n-input type="text" v-model:value="conf.host" placeholder="Server name or IP address" :disabled="readonly"
                ref="host"></n-input>
        </n-gi>
        <n-gi>
            <span>Port</span>
            <n-input type="number" v-model:value.number="conf.port" placeholder="Server port" :disabled="readonly"
                ref="port"></n-input>
        </n-gi>
        <n-gi>
            <span>Login</span>
            <n-input type="text" v-model:value="conf.login" placeholder="Login name, anonymous by default"
                :disabled="readonly" ref="login"></n-input>
        </n-gi>
        <n-gi>
            <span>Password</span>
            <n-input type="password" v-model:value="conf.password" placeholder="Password" :disabled="readonly"
                ref="password"></n-input>
        </n-gi>
        <n-gi>
            <span>Remote Path</span>
            <n-input type="text" v-model:value="conf.remotePath" placeholder="Path to store file in server"
                :disabled="readonly" ref="rpath"></n-input>
        </n-gi>
        <n-gi>
            <span>Remote Name</span>
            <n-input type="text" v-model:value="conf.remoteName" placeholder="File name to store in server"
                :disabled="readonly"></n-input>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="conf.passive" :disabled="readonly">Passive</n-checkbox>
        </n-gi>
        <n-gi>
            <n-checkbox v-model:checked="conf.binaryTransport" :disabled="readonly">Binary Transport</n-checkbox>
        </n-gi>
        <n-gi>
            <n-button type="primary" :disabled="readonly" @click="verifyFtp">Verify</n-button>
        </n-gi>
        <n-gi>
            <span :class="ftpmsgstyle">{{ ftpMessage.text }}</span>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.ftpsuccess {
    color: green;
}

.ftpfail {
    color: red;
}
</style>
