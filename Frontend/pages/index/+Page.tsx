import React from "react";
import FileInput from "../../components/FileInput/FileInput";
import {parseImage} from "../../api/api";

export default function Page() {
    const [file, setFile] = React.useState<File | null>(null);
    const [loading, setLoading] = React.useState<boolean>(false);
    const [error, setError] = React.useState<string | null>(null);
    const [response, setResponse] = React.useState<IPictureData | null>(null);
    const onFileChange = (files: FileList) => {
        if (files[0]) {
            setFile(files[0]);
        }
    };

    React.useEffect(() => {
        if (file) {
            setLoading(true);
            parseImage(file).then((response) => {
                setResponse(response);
                setLoading(false);
            }).catch((error) => {
                setError(error);
                setLoading(false);
            });
        }
    }, [file]);

    if (loading) {
        return <div>Loading...</div>;
    }
    if (error) {
        return <div>Error: {JSON.stringify(error)}</div>;
    }
    if (!response)
        return <FileInput onFileChange={onFileChange}/>
    return (
        <>
            <FileInput onFileChange={onFileChange}/>
            <table className={'table-auto border'}>
                <thead>
                <tr>
                    <th>Дата</th>
                    <th>Событие</th>
                    <th>Основание</th>
                </tr>
                </thead>
                <tbody>
                {Object.entries(response).map(([key, value]) => (
                    <tr key={key} className={`border-b ${value[0].length > 0 ? 'border-t-2 border-t-neutral-600' : ''}`}>
                        <td><span className={'mx-2'}>{value[0]}</span></td>
                        <td><span className={'mx-2'}>{value[1]}</span></td>
                        <td><span className={'mx-2'}>{value[2]}</span></td>
                    </tr>
                ))}
                </tbody>
            </table>
        </>
    );
}
