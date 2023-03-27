<script lang="ts">
import type { UploadedFileInfo } from '@/api/uploadapi';
import type { ExcelInfo } from '@/entities/datafile/ExcelInfo';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        fileInfo: {
            type: Object as PropType<UploadedFileInfo>,
            required: true
        }
    },
    setup(props) {
        let info = props.fileInfo.extra as ExcelInfo
        return reactive({
            info,
            sheets: info.sheets.map((sh, i) => ({ label: sh.name, value: i })),
            sheetIndex: 0
        })
    }
})
</script>

<template>
    <div>Excel File</div>
    <n-input-group>
        <div class="sheet">Sheet</div>
        <n-select :options="sheets" v-model:value="sheetIndex"></n-select>
    </n-input-group>
</template>

<style scoped>
.sheet {
    width: 120px;
}
</style>
