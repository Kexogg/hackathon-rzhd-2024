import React from "react";
import FileInput from "../../components/FileInput/FileInput";
import {sendImage} from "../../api/api";

export default function Page() {
    const [files, setFiles] = React.useState<File[]>([]);
    const [response, setResponse] = React.useState<any>(null);
    const [loading, setLoading] = React.useState<boolean>(false);
    const onFileChange = (files: FileList) => {
        setFiles(Array.from(files));
        console.log(files);
    };
    const sendFiles = async () => {
        for (const file of files) {
            setLoading(true);
            const response = await sendImage(file).catch((e) => {
                console.error(e);
                setLoading(false);
            });
            setLoading(false)
            console.log(response);
            setResponse(response);
        }
    }
    if (loading) {
        return <div>Loading...</div>;
    }
    return (
        <>
            <h1 className="font-bold text-3xl pb-4">Оцифровка трудовых книжек</h1>
            {!response ?
                <>
                    <FileInput onFileChange={onFileChange}/>
                    <button className="bg-blue-500 text-white p-2 rounded-lg" onClick={() => sendFiles()}>Отправить
                    </button>
                </>
                :
                <div>{response.status}</div>
            }
        </>
    );
}
