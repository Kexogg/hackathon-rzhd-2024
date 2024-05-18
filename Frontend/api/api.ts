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
