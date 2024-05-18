import React from "react";
import FileInput from "../../components/FileInput/FileInput";
import {sendImage} from "../../api/api";

export default function Page() {
    const [files, setFiles] = React.useState<File[]>([]);
    const onFileChange = (files: FileList) => {
        setFiles(Array.from(files));
        console.log(files);
    };
    const sendFiles = async () => {
        for (const file of files) {
            const response = await sendImage(file);
            console.log(response);
        }
    }
    return (
        <>
            <h1 className="font-bold text-3xl pb-4">Оцифровка трудовых книжек</h1>
            <FileInput onFileChange={onFileChange}/>
            {files.length}
            <button className="bg-blue-500 text-white p-2 rounded-lg" onClick={() => sendFiles()}>Отправить</button>
        </>
    );
}
