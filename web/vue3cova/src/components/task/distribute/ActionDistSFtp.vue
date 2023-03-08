<script lang="ts">
import verificationApi from '@/api/verificationapi';
import type DistSFtpConfiguration from '@/entities/task/distribute/distconfigsftp';
import { newDistSFtpConfiguration } from '@/entities/task/distribute/distconfigsftp';
import type { Task } from '@/entities/task/task';
import type { TaskActionDistribute } from '@/entities/task/taskdist';
import { identityStore } from '@/stores/identitystore';
import type { UploadCustomRequestOptions } from 'naive-ui/es/upload';
import { computed, defineComponent, reactive, type PropType } from 'vue';

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
        let conf = newDistSFtpConfiguration(props.action.configuration)
        return reactive({
            conf,
            sFtpMessage: {
                success: false,
                text: ""
            },
            sftpmsgstyle: "sftpsuccess",
            authOptions: [
                { label: "Password", value: "Password" },
                { label: "PrivateKey", value: "PrivateKey" }
            ],
            port: computed({
                get: () => (conf.port.toString()),
                set: (v: any) => {
                    //console.log(typeof v, JSON.stringify(v))
                    if (typeof v !== 'string') {
                        return
                    }
                    conf.port = parseInt(v)
                }
            }),
            privateKeyUpload: null as UploadCustomRequestOptions | null
        })
    },
    unmounted() {
        this.sFtpMessage.text = ""
    },
    methods: {
        verifySFtp() {
            if (this.conf.authType === 'Password') {
                this.verifySFtpPassword()
            } else {
                this.verifySFtpPrivateKey()
            }
        },
        verifySFtpPassword() {
            if (!this.sFtpParamsCheck(this.conf)) {
                return
            }
            let user = identityStore().currentIdentity()
            verificationApi.verifySFtp(user, this.conf).then(rsp => {
                this.showMessage(true, `Ftp working directory is ${rsp.data.pwd}`)
            }).catch(err => {
                this.showMessage(false, err)
            })
        },
        showMessage(r: boolean, t: string) {
            this.sFtpMessage = {
                success: r,
                text: t
            }
            this.sftpmsgstyle = r ? "sftpsuccess" : "sftpfail"
        },
        sFtpParamsCheck(config: DistSFtpConfiguration): boolean {
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
            if (config.authType === 'Password') {
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
            } else {
                if ((config.privateKeyFile === undefined) || (config.privateKeyFile === null)) {
                    this.showMessage(false, "private key is missing")
                    return false
                }
            }
            return true
        },
        sslectPrivateKey(uploadOptions: UploadCustomRequestOptions) {
            this.privateKeyUpload = uploadOptions
        },
        verifySFtpPrivateKey() {
            let user = identityStore().currentIdentity()
            if (this.privateKeyUpload === null) {
                this.showMessage(false, "Private Key file Not selected")
                return
            }
            let up = this.privateKeyUpload!
            verificationApi.verifySFtpWithKey(user, this.conf, up.file.file!).then(rsp => {
                this.conf.privateKeyFile = rsp.data.keyStored
                this.showMessage(true, `Verification with private key success, SFtp working directory is ${rsp.data.pwd}`)
                up.onFinish()
            }).catch(err => {
                this.showMessage(false, err)
                up.onError()
            })
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
            <n-input type="number" v-model:value.number="port" placeholder="Server port" :disabled="readonly"
                ref="port"></n-input>
        </n-gi>
        <n-gi>
            <span>Login</span>
            <n-input type="text" v-model:value="conf.login" placeholder="Login name" :disabled="readonly"
                ref="login"></n-input>
        </n-gi>
        <n-gi v-if="conf.authType === 'Password'">
            <span>Password</span>
            <n-input type="password" v-model:value="conf.password" placeholder="Password" :disabled="readonly"
                ref="password"></n-input>
        </n-gi>
        <n-gi span="2">
            <span style="margin-right: 5px">AuthType</span>
            <n-popselect :options="authOptions" v-model:value="conf.authType">
                <n-button>{{ conf.authType }}</n-button>
            </n-popselect>
        </n-gi>
        <n-gi v-if="conf.authType === 'PrivateKey'">
            <span>Private Key File</span>
            <n-upload :custom-request="sslectPrivateKey">
                <n-button>上传文件</n-button>
            </n-upload>
        </n-gi>
        <n-gi v-if="conf.authType === 'PrivateKey'">
            <span>Pass Phrase for Key</span>
            <n-input type="text" :value="conf.passPhrase" :disabled="readonly"></n-input>
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
            <n-button type="primary" :disabled="readonly" @click="verifySFtp">Verify</n-button>
        </n-gi>
        <n-gi>
            <span :class="sftpmsgstyle">{{ sFtpMessage.text }}</span>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.sftpsuccess {
    color: green;
}

.sftpfail {
    color: red;
}
</style>
