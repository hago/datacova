<script lang="ts">
import type { UploadedFileInfo } from '@/api/uploadapi';
import { Charsets } from '@/entities/charsets';
import type { CsvFileInfo } from '@/entities/datafile/CsvFileInfo';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        fileInfo: {
            type: Object as PropType<UploadedFileInfo>,
            required: true
        }
    },
    setup(props) {
        let info = props.fileInfo.extra as CsvFileInfo
        return reactive({
            encodings: Charsets.map(c => ({ label: c, value: c })),
            info
        })
    }
})
</script>

<template>
    <h1>CSV file</h1>
    <n-grid cols="2">
        <n-gi>
            <n-input-group>
                <div :bordered="false" class="attrname">Quote</div>
                <n-input type="text" placeholder='"' v-model:value="info.quote" />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <div :bordered="false" class="attrname">Delimiter</div>
                <n-input type="text" placeholder=',' v-model:value="info.delimiter" />
            </n-input-group>
        </n-gi>
        <n-gi>
            <n-input-group>
                <div :bordered="false" class="attrname">Delimiter</div>
                <n-select :options="encodings" filterable v-model:value="info.encoding"
                    :fallback-option="(v: string | number) => ({ label: 'auto', value: null })" />
            </n-input-group>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.attrname {
    color: cadetblue;
    width: 100px;
    text-align: center;
    margin-top: 0;
    margin-bottom: 0;
}
</style>
