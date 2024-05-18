import React, {useState} from 'react';

type FileInputProps = {
    onFileChange: (files: FileList) => void;
};


const FileInput = ({onFileChange}: FileInputProps) => {
    const [dragging, setDragging] = useState(false);
    const fileInputRef = React.createRef<HTMLInputElement>();

    const dragOverHandler = (event: React.DragEvent) => {
        event.preventDefault();
        console.log('dragging over');
        setDragging(true);
    };

    const dragLeaveHandler = (event: React.DragEvent) => {
        event.preventDefault();
        console.log('dragging leave');
        setDragging(false);
    };

    const dropHandler = (event: React.DragEvent) => {
        event.preventDefault();
        console.log('dropped');
        setDragging(false);

        if (event.dataTransfer.items) {
            let files: File[] = [];
            for (const element of event.dataTransfer.items) {
                if (element.kind === 'file') {
                    const file = element.getAsFile();
                    files.push(file as File);
                }
            }
            if (fileInputRef.current && files.length > 0) {
                const dataTransfer = new DataTransfer();
                for (let file of files) {
                    dataTransfer.items.add(file);
                }
                fileInputRef.current.files = dataTransfer.files;
            }
        }
    };

    return (
        <div
            className={`w-full relative h-24 bg-neutral-100 p-1 ${dragging ? 'bg-blue-200' : ''}`}>
            <input type="file"
                     onChange={(event) => {
                          onFileChange(event.target.files as FileList);
                     }}
                   ref={fileInputRef} className={'h-full w-full'}/>
            <div
                onDragEnter={dragOverHandler}
                onDragOver={dragOverHandler} onDragLeave={dragLeaveHandler} onDrop={dropHandler}
                className={'p-3 w-full h-full outline-dashed outline-neutral-400 outline-4 flex flex-col items-center justify-center gap-3'}>
                <span className={'block'}>Перетащите файлы сюда или нажмите чтобы выбрать файлы</span>
                <button
                    className={'block w-fit bg-neutral-300 p-1.5 rounded-lg hover:bg-neutral-200 border border-neutral-600'}
                    onClick={() => {
                        if (fileInputRef.current) {
                            fileInputRef.current.click();
                        }
                    }}>Загрузить файл
                </button>
            </div>
        </div>
    );
};

export default FileInput;
