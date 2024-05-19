const convertImageToBase64 = (image: File): Promise<string | ArrayBuffer | null> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(image);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

export const parseImage = async (image: File) => {
    const base64 = await convertImageToBase64(image);
    return fetch('/api/upload', {
        method: 'POST',
        body: JSON.stringify({image: base64}),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.json()).catch(error => error);
}

export const updateImageData = async (data: IPictureData) => {
    return fetch('/api/image/' + data.imageId, {
        method: 'PUT',
        body: JSON.stringify({imageText: data.data}),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.json()).catch(error => error);
}

export const getImageData = async (imageId: string) => {
    return fetch('/api/image/' + imageId).then(response => response.json()).catch(error => error);
}
