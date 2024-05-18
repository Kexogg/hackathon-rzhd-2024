export const sendImage = async (image: File) => {
    const reader = new FileReader();
    reader.readAsDataURL(image);
    return new Promise((resolve) => {
        reader.onload = async () => {
            const base64 = reader.result;
            const response = await fetch(`api/upload`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({image: base64})
            });
            resolve(response);
        };
        reader.onerror = error => console.error(error);
    });
}
