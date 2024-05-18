import React from "react";
import FileInput from "../../components/FileInput/FileInput";

export default function Page() {
    const [files, setFiles] = React.useState<File[]>([]);
    const onFileChange = (files: FileList) => {
        setFiles(Array.from(files));
        console.log(files);
    };
    return (
        <>
            <h1 className="font-bold text-3xl pb-4">Оцифровка трудовых книжек</h1>
            <FileInput onFileChange={onFileChange}/>
            {files.length}
        </>
    );
}
