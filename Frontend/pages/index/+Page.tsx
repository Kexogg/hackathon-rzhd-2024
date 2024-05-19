import React, {useEffect, useState} from "react";
import FileInput from "../../components/FileInput/FileInput";
import {getImageData, parseImage, updateImageData} from "../../api/api";
import Button from "../../components/Button/Button";
import {SubmitHandler, useForm} from "react-hook-form";
import Searchbar from "../../components/Searchbar/Searchbar";

export default function Page() {
    const [file, setFile] = useState<File | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [response, setResponse] = useState<IPictureData | null>(null);
    const {register, handleSubmit, setValue} = useForm<IPictureData>();

    const onFileChange = (files: FileList) => {
        if (files[0]) {
            setFile(files[0]);
        }
    };

    useEffect(() => {
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

    useEffect(() => {
        if (response) {
            response.data.forEach((item, index) => {
                setValue(`data.${index}.field1`, item.field1);
                setValue(`data.${index}.field2`, item.field2);
                setValue(`data.${index}.field3`, item.field3);
            });
        }
    }, [response, setValue]);

    const DownloadJSON = () => {
        const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(response));
        const downloadAnchorNode = document.createElement('a');
        downloadAnchorNode.setAttribute("href", dataStr);
        downloadAnchorNode.setAttribute("download", "data.json");
        document.body.appendChild(downloadAnchorNode);
        downloadAnchorNode.click();
        downloadAnchorNode.remove();
    }

    const onSubmit: SubmitHandler<IPictureData> = (data: IPictureData) => {
        console.log(data);
        setResponse((r) => ({...r, ...data}));
        updateImageData({...response, ...data}).then((response) => {
            console.log(response);
        }).catch((error) => {
            setError(error)
            console.log(error);
        });
    };

    const onSearchSubmit = (search: string) => {
        console.log(search);
        setLoading(true)
        getImageData(search).then((response) => {
            setResponse(response);
        }).catch((error) => {
            setError(error)
            console.log(error);
        }).finally(() => setLoading(false));
    }

    const header = (
        <>
            <SearchbarBlock onSearchSubmit={onSearchSubmit}/>
            <FileInput onFileChange={onFileChange}/>
        </>
    )

    if (loading) {
        return <div>Загрузка...</div>;
    }
    if (error) {
        return <div>Ошибка: {JSON.stringify(error)}</div>;
    }
    if (!response)
        return <>{header}</>;
    return (
        <div className={'flex flex-col gap-3'}>
            {header}
            <img src={response.s3Link} alt={'Картинка трудовой книжки'} className={'w-1/2'}/>
            <form onSubmit={handleSubmit(onSubmit)}>
                <table className={'table table-auto border w-full'}>
                    <thead>
                    <tr>
                        <th>Дата</th>
                        <th>Событие</th>
                        <th>Основание</th>
                    </tr>
                    </thead>
                    <tbody>
                    {response.data.map((item, index) => (
                        <tr key={index}
                            className={`border-b ${item.field1.length > 0 ? 'border-t-2 border-t-neutral-600' : ''}`}>
                            <td><input className={'mx-2 w-full'} {...register(`data.${index}.field1`)}
                                       defaultValue={item.field1}/>
                            </td>
                            <td><input className={'mx-2 w-full'} {...register(`data.${index}.field2`)}
                                       defaultValue={item.field2}/>
                            </td>
                            <td><input className={'mx-2 w-full'} {...register(`data.${index}.field3`)}
                                       defaultValue={item.field3}/>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
                <Button>Сохранить изменения</Button>
            </form>
            <Button type={'button'} onClick={() => DownloadJSON()}>Скачать JSON</Button>
        </div>
    );
}


const SearchbarBlock = ({onSearchSubmit}: { onSearchSubmit: (search: string) => void }) => {
    const [search, setSearch] = useState<string>('');
    return (<form onSubmit={() => onSearchSubmit(search)} className={'flex w-full gap-3'}>
        <Searchbar
            onChange={(event) => setSearch(event.target.value)}
            placeholder={'ID документа'}/>
        <Button>Поиск</Button>
    </form>)
}
